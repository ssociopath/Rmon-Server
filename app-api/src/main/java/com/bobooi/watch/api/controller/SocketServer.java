package com.bobooi.watch.api.controller;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author bobo
 * @date 2021/6/25
 */

public class SocketServer {

    public void connect() throws IOException, InterruptedException {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) {
                        ch.pipeline().addLast(new IdleStateHandler(10, 0, 0, TimeUnit.SECONDS));
                        ch.pipeline().addLast(new FirstServerHandler());
                    }
                });

        serverBootstrap.bind(5555).sync();
    }

    static class FirstServerHandler extends ChannelInboundHandlerAdapter {
        private int unRecPingTimes = 0;
        private static final int MAX_UN_REC_PING_TIMES = 3;
        private Random random = new Random(System.currentTimeMillis());

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            if (evt instanceof IdleStateEvent) {
                IdleStateEvent event = (IdleStateEvent) evt;
                if (event.state()== IdleState.READER_IDLE){
                    System.out.println("===服务端===(READER_IDLE 读超时)");
                    // 失败计数器次数大于等于3次的时候，关闭链接，等待client重连
                    if (unRecPingTimes >= MAX_UN_REC_PING_TIMES) {
                        System.out.println("===服务端===(读超时，关闭chanel)");
                        // 连续超过N次未收到client的ping消息，那么关闭该通道，等待client重连
                        ctx.close();
                    } else {
                        // 失败计数器加1
                        unRecPingTimes++;
                    }
                }else {
                    super.userEventTriggered(ctx,evt);
                }
            }
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            ByteBuf byteBuf = (ByteBuf) msg;
            String msgStr = byteBuf.toString(StandardCharsets.UTF_8);
            System.out.println(new Date() + msgStr);
            if("heart".equals(msgStr)){
                System.out.println("服务端收到心跳连接");
            }else{
                System.out.println("服务端读到数据 -> " + msgStr);
                //接收到客户端的消息后我们再回复客户端
                ByteBuf out = getByteBuf(ctx);
                ctx.channel().writeAndFlush(out);
            }
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            super.channelActive(ctx);
            System.out.println("客户端"+ctx.channel().remoteAddress()+"已连接");
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            super.channelInactive(ctx);
            System.out.println("客户端"+ctx.channel().remoteAddress()+"已断开");
        }

        private ByteBuf getByteBuf(ChannelHandlerContext ctx) {
            byte[] bytes = "服务器:我是服务器，我收到你的消息了！".getBytes(StandardCharsets.UTF_8);
            ByteBuf buffer = ctx.alloc().buffer();
            buffer.writeBytes(bytes);
            return buffer;
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        SocketServer socketServer = new SocketServer();
        socketServer.connect();
    }

}
