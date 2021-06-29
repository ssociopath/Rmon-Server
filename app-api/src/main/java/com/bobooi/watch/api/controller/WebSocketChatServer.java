package com.bobooi.watch.api.controller;

import com.bobooi.watch.api.protocol.vo.WsMessage;
import com.bobooi.watch.common.utils.JsonUtil;
import com.bobooi.watch.common.utils.misc.Constant;
import com.bobooi.watch.data.entity.Pc;
import com.bobooi.watch.data.service.concrete.PcService;
import com.bobooi.watch.data.service.concrete.RuleService;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.dfa.DFA;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author bobo
 * @date 2021/6/22
 */

@Slf4j
@Component
@ServerEndpoint("/{userId}")
public class WebSocketChatServer {
    private static Map<String, Session> ONLINE_SESSIONS = new ConcurrentHashMap<>();
    private static Map<String, WsMessage> CONNECT_SESSIONS = new ConcurrentHashMap<>();
    @Resource
    SocketServer socketServer;
    @Resource
    private RuleService ruleService;
    @Resource
    private PcService pcService;
    private static WebSocketChatServer webSocketChatServer;

    @PostConstruct
    public void init() {
        webSocketChatServer = this;
        webSocketChatServer.pcService = this.pcService;
        webSocketChatServer.ruleService = this.ruleService;
        webSocketChatServer.socketServer = this.socketServer;
    }


    /**
     * 当客户端打开连接：1.添加会话对象 2.更新在线人数
     */
    @OnOpen
    public void onOpen(Session session,@PathParam("userId") String userId) {
        ONLINE_SESSIONS.put(userId, session);
        System.out.println("成功连接，在线人数："+ ONLINE_SESSIONS.size());
    }


    @OnMessage
    public void onMessage(String jsonStr) throws IOException {
        System.out.println(jsonStr);
        WsMessage wsMessage = JsonUtil.parseObject(jsonStr, WsMessage.class);
        if(Constant.WS_OPEN.equals(wsMessage.getType())){

        }else{
            Pc pc = webSocketChatServer.pcService.findOneByMac(wsMessage.getToUserId());
            if(pc!=null && webSocketChatServer.ruleService.findOneByUserIdAndPcId(Integer.valueOf(wsMessage.getFromUserId()),pc.getId())!=null){
                wsMessage.setToUserId(String.valueOf(pc.getId()));
                String content = JsonUtil.toJsonString(wsMessage);
                CONNECT_SESSIONS.put(wsMessage.getFromUserId(),wsMessage);
                webSocketChatServer.socketServer.sendMsg(pc.getId(),Constant.IMAGE,Constant.RESPONSE_SUCCEED,
                        -1, Constant.DF,content.getBytes(StandardCharsets.UTF_8));
            }
        }
        log.info("收到消息："+ wsMessage);
    }

    public static void sendMsg(WsMessage wsMessage) throws IOException {
        if(ONLINE_SESSIONS.containsKey(wsMessage.getFromUserId())){
            ONLINE_SESSIONS.get(wsMessage.getFromUserId()).getBasicRemote().sendText(JsonUtil.toJsonString(wsMessage));
        }
    }

    /**
     * 当关闭连接：1.移除会话对象 2.更新在线人数
     */
    @OnClose
    public void onClose(@PathParam("userId") String userId) {
        WsMessage wsMessage = CONNECT_SESSIONS.get(userId);
        ONLINE_SESSIONS.remove(userId);
        CONNECT_SESSIONS.remove(userId);
        webSocketChatServer.socketServer.sendMsg(Integer.valueOf(wsMessage.getToUserId()),Constant.LOGOUT,
                Constant.RESPONSE_SUCCEED, -2, Constant.DF,"关闭监控".getBytes());
        System.out.println("关闭连接，在线人数："+ ONLINE_SESSIONS.size());
    }

    /**
     * 当通信发生异常：打印错误日志
     */
    @OnError
    public void onError(Session session, Throwable error) {
        error.printStackTrace();
    }

}
