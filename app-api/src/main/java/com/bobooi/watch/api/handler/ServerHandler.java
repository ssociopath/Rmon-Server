package com.bobooi.watch.api.handler;

import com.bobooi.watch.api.controller.SocketServer;
import com.bobooi.watch.api.controller.WebSocketChatServer;
import com.bobooi.watch.api.protocol.vo.RequestPacket;
import com.bobooi.watch.api.protocol.vo.ResponsePacket;
import com.bobooi.watch.api.protocol.vo.RuleVO;
import com.bobooi.watch.api.protocol.vo.WsMessage;
import com.bobooi.watch.common.utils.JsonUtil;
import com.bobooi.watch.common.utils.misc.Constant;
import com.bobooi.watch.data.entity.Pc;
import com.bobooi.watch.data.service.concrete.PcService;
import com.bobooi.watch.data.service.concrete.RuleService;
import com.mysql.cj.protocol.Message;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author bobo
 * @date 2021/6/28
 */

@Component
public class ServerHandler extends ChannelInboundHandlerAdapter {
    @Resource
    public PcService pcService;
    @Resource
    public RuleService ruleService;
    private static ServerHandler  serverHandler;
    private int unRecPingTimes = 0;
    private String clientAddr="";
    private Integer pcId;
    private static final int MAX_UN_REC_PING_TIMES = 3;
    private static Map<Integer,byte[]> pkgList = new HashMap<>();

    @PostConstruct
    public void init() {
        serverHandler = this;
        serverHandler.pcService = this.pcService;
        serverHandler.ruleService = this.ruleService;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state()== IdleState.READER_IDLE){
                System.out.println("===服务端===(READER_IDLE 读超时)");
                if (unRecPingTimes >= MAX_UN_REC_PING_TIMES) {
                    System.out.println("===服务端===(读超时，关闭chanel)");
                    ctx.close();
                } else {
                    unRecPingTimes++;
                }
            }else {
                super.userEventTriggered(ctx,evt);
            }
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws IOException {
        RequestPacket requestPacket = (RequestPacket)msg;
        byte[] part = requestPacket.getContent();
        int id = requestPacket.getId();
        if(pkgList.containsKey(requestPacket.getId())){
            byte[] prePart = pkgList.get(id);
            ByteBuffer byteBuffer = ByteBuffer.allocate(prePart.length+part.length);
            byteBuffer.put(prePart);
            byteBuffer.put(part);
            byte[] nowPart = byteBuffer.array();
            if(Constant.DF== requestPacket.getFlag()){
                handleData(ctx,id, requestPacket.getType(),nowPart);
                pkgList.remove(id);
            }else{
                pkgList.put(id,nowPart);
            }
        }else{
            if(Constant.MF== requestPacket.getFlag()){
                pkgList.put(id, requestPacket.getContent());
            }else{
                handleData(ctx,id, requestPacket.getType(), requestPacket.getContent());
            }
        }
    }

    @Async
    public void handleData(ChannelHandlerContext ctx, int id, byte type, byte[] content) throws IOException {
        ResponsePacket responsePack = new ResponsePacket();
        responsePack.setType(type);
        responsePack.setId(id+1);
        String contentStr = new String(content,StandardCharsets.UTF_8);
        String response = "收到客户端 "+clientAddr + " 消息：" +
                (type==Constant.IMAGE?("图片:seq="+id):contentStr);

        switch (type){
            case Constant.HEART:
                responsePack = new ResponsePacket();
                responsePack.setContent(("心跳连接:seq="+ id).getBytes());
                responsePack.setResult(Constant.RESPONSE_SUCCEED);
                ctx.writeAndFlush(responsePack);
                break;
            case Constant.LOGIN:
                Pc pc = JsonUtil.parseObject(contentStr, Pc.class);
                Pc thePc = serverHandler.pcService.login(pc);
                if(thePc==null){
                    responsePack.setContent("用户或密码错误".getBytes());
                    responsePack.setResult(Constant.RESPONSE_FAIL);
                }else{
                    pcId = thePc.getId();
                    responsePack.setContent(JsonUtil.toJsonString(
                            serverHandler.ruleService.findAllByPcId(pcId)
                                    .stream()
                                    .map(RuleVO::fromRule)
                                    .collect(Collectors.toList()))
                                    .getBytes(StandardCharsets.UTF_8));
                    SocketServer.LOGIN_CHANNELS.put(pcId,ctx.channel());
                    responsePack.setResult(Constant.RESPONSE_SUCCEED);
                }
                ctx.writeAndFlush(responsePack);
                break;
            case Constant.DATA_UPDATE:
                RuleVO ruleVO = JsonUtil.parseObject(new String(content, StandardCharsets.UTF_8),RuleVO.class);
                if(serverHandler.ruleService.update(ruleVO.getRuleId()==null?null:ruleVO.getRuleId(), pcId,
                        ruleVO.getAccount(), ruleVO.getBytePermission())){
                    responsePack.setContent(JsonUtil.toJsonString(
                            serverHandler.ruleService.findAll()
                                    .stream()
                                    .map(RuleVO::fromRule)
                                    .collect(Collectors.toList())).getBytes());
                    responsePack.setResult(Constant.RESPONSE_SUCCEED);
                }else{
                    responsePack.setContent("更新失败".getBytes());
                    responsePack.setResult(Constant.RESPONSE_FAIL);
                }
                ctx.writeAndFlush(responsePack);
                break;
            case Constant.DATA_DELETE:
                Integer ruleId = Integer.valueOf(contentStr);
                if(serverHandler.ruleService.deleteById(ruleId)){
                    responsePack.setContent(JsonUtil.toJsonString(
                            serverHandler.ruleService.findAll()
                                    .stream()
                                    .map(RuleVO::fromRule)
                                    .collect(Collectors.toList())).getBytes());
                    responsePack.setResult(Constant.RESPONSE_SUCCEED);
                }else{
                    responsePack.setContent("删除失败".getBytes());
                    responsePack.setResult(Constant.RESPONSE_FAIL);
                }
                ctx.writeAndFlush(responsePack);
                break;
            case Constant.LOGOUT:
                // TODO 处理登出逻辑
                responsePack.setContent(("登出信息:seq="+ id).getBytes());
                break;
            case Constant.IMAGE:
                responsePack.setContent(("图片消息:seq="+ id).getBytes());
                WsMessage wsMessage = JsonUtil.parseObject(contentStr, WsMessage.class);
                WebSocketChatServer.sendMsg(wsMessage);
                break;
            default:
                responsePack.setContent(("不支持的请求方式:seq="+ id).getBytes());
                break;
        }
        System.out.println(response);
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        clientAddr = String.valueOf(ctx.channel().remoteAddress());
        SocketServer.ONLINE_CHANNELS.put(clientAddr,ctx.channel());
        System.out.println("客户端 "+clientAddr+" 已连接");

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        SocketServer.ONLINE_CHANNELS.remove(clientAddr);
        SocketServer.LOGIN_CHANNELS.remove(pcId);
        System.out.println("客户端 "+clientAddr+" 已断开");
    }
}