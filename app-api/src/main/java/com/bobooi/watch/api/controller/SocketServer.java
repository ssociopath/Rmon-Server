package com.bobooi.watch.api.controller;

import com.bobooi.watch.api.handler.ServerHandler;
import com.bobooi.watch.api.protocol.vo.ResponsePacket;
import com.bobooi.watch.api.protocol.MsgPackDecoder;
import com.bobooi.watch.api.protocol.MsgPackEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author bobo
 * @date 2021/6/25
 */

@Component
public class SocketServer implements CommandLineRunner {

    public static final Map<String, Channel> ONLINE_CHANNELS = new ConcurrentHashMap<>();
    public static final Map<Integer, Channel> LOGIN_CHANNELS  = new ConcurrentHashMap<>();

    public boolean sendMsg(Integer pcId,byte type,byte result, int id,byte flag,byte[] content){
        Channel socketChannel = LOGIN_CHANNELS.get(pcId);
        if(socketChannel != null){
            if(socketChannel.isOpen() || socketChannel.isActive()){
                socketChannel.writeAndFlush(new ResponsePacket(type,result,id,flag,content));
                return true;
            }
        }
        return false;
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

}
