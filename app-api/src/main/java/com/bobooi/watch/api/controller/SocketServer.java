package com.bobooi.watch.api.controller;

import com.bobooi.watch.api.protocol.MsgPack;
import com.bobooi.watch.api.protocol.MsgPackDecoder;
import com.bobooi.watch.api.protocol.MsgPackEncoder;
import com.bobooi.watch.common.utils.misc.Constant;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author bobo
 * @date 2021/6/25
 */

@Component
public class SocketServer implements CommandLineRunner {
    private static Map<String, Channel> onlineSessions = new ConcurrentHashMap<>();

    public void sendMsg(byte type,int id,byte flag,byte[] content){
        onlineSessions.forEach(((s, socketChannel) -> {
            if(socketChannel.isOpen() || socketChannel.isActive()){
                socketChannel.writeAndFlush(new MsgPack(type,id,flag,content));
            }
        }));
    }

    @Override
    public void run(String... args) throws Exception {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        try {
            serverBootstrap
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) {
                            ch.pipeline().addLast(new IdleStateHandler(10, 0, 0, TimeUnit.SECONDS))
                                    .addLast(new LengthFieldBasedFrameDecoder(65535, 0,4,0,4))
                                    .addLast(new MsgPackDecoder())
                                    .addLast(new LengthFieldPrepender(4))
                                    .addLast(new MsgPackEncoder())
                                    .addLast(new ServerHandler());
                        }
                    });
            ChannelFuture f = serverBootstrap.bind(5555).sync();
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    static class ServerHandler extends ChannelInboundHandlerAdapter {
        private int unRecPingTimes = 0;
        private String clientAddr="";
        private static final int MAX_UN_REC_PING_TIMES = 3;
        private static Map<Integer,byte[]> pkgList = new HashMap<>();

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
            MsgPack msgPack = (MsgPack)msg;
            byte[] part = msgPack.getContent();
            int id = msgPack.getId();
            if(pkgList.containsKey(msgPack.getId())){
                byte[] prePart = pkgList.get(id);
                ByteBuffer byteBuffer = ByteBuffer.allocate(prePart.length+part.length);
                byteBuffer.put(prePart);
                byteBuffer.put(part);
                byte[] nowPart = byteBuffer.array();
                if(Constant.DF==msgPack.getFlag()){
                    handleData(ctx,id,msgPack.getType(),nowPart);
                    pkgList.remove(id);
                }else{
                    pkgList.put(id,nowPart);
                }
            }else{
                if(Constant.MF==msgPack.getFlag()){
                    pkgList.put(id, msgPack.getContent());
                }else{
                    handleData(ctx,id,msgPack.getType(),msgPack.getContent());
                }
            }
        }

        public void handleData(ChannelHandlerContext ctx, int id, byte type, byte[] content) throws IOException {
            String response = "收到客户端 "+clientAddr + " ";
            MsgPack responsePack;
            switch (type){
                case Constant.HEART:
                    response  += "心跳连接:seq="+ id;
                    break;
                case Constant.LOGIN:
                    // TODO 处理登录逻辑
                    response += "登录信息:seq="+ id;
                    break;
                case Constant.LOGOUT:
                    // TODO 处理登出逻辑
                    response  += "登出信息:seq="+ id;
                    break;
                case Constant.TEXT:
                    // TODO 处理文本逻辑
                    response += "文本消息:seq="+ id;
                    break;
                case Constant.IMAGE:
                    // TODO 处理图片逻辑
                    response += "图片消息:seq="+ id;
                    FileOutputStream out = new FileOutputStream("test.jpg");
                    out.write(content);
                    out.close();
                    WebSocketChatServer.sendMsg(clientAddr,"IMAGE",Base64.getEncoder().encodeToString(content));
                    break;
                default:
                    System.out.println("错误类型！");
                    break;
            }
            System.out.println(response);
            responsePack = new MsgPack(Constant.TEXT,id+1,Constant.DF,response.getBytes());
            ctx.writeAndFlush(responsePack);
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            super.channelActive(ctx);
            clientAddr = String.valueOf(ctx.channel().remoteAddress());
            onlineSessions.put(clientAddr,ctx.channel());
            System.out.println("客户端 "+clientAddr+" 已连接");

        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            super.channelInactive(ctx);
            onlineSessions.remove(clientAddr);
            System.out.println("客户端 "+clientAddr+" 已断开");
        }
    }

}
