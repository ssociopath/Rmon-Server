package com.bobooi.watch.api.controller;

import com.bobooi.watch.api.protocol.vo.WsMessage;
import com.bobooi.watch.common.utils.JsonUtil;
import com.bobooi.watch.common.utils.misc.Constant;
import com.bobooi.watch.data.entity.Rule;
import com.bobooi.watch.data.entity.User;
import com.bobooi.watch.data.service.concrete.PcService;
import com.bobooi.watch.data.service.concrete.RuleService;
import com.bobooi.watch.data.service.concrete.UserService;
import lombok.extern.slf4j.Slf4j;
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
@ServerEndpoint("/ws/{account}")
public class WebSocketChatServer {
    private static Map<String, Session> ONLINE_SESSIONS = new ConcurrentHashMap<>();
    private static Map<String, Rule> CONNECT_SESSIONS = new ConcurrentHashMap<>();
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
    public void onOpen(Session session,@PathParam("account") String userId) {
        System.out.println(userId);
        ONLINE_SESSIONS.put(userId, session);
        System.out.println("成功连接，在线人数："+ ONLINE_SESSIONS.size());
    }


    @OnMessage
    public void onMessage(Session session, String jsonStr) throws IOException {
        WsMessage wsMessage = JsonUtil.parseObject(jsonStr, WsMessage.class);
        String type = wsMessage.getType();
        String from = wsMessage.getFrom();
        String to = wsMessage.getTo();
        String content = wsMessage.getContent();

        switch (type){
            case Constant.WS_CONNECT:
                Rule rule  = JsonUtil.parseObject(content,Rule.class);
                wsMessage.setContent("连接失败，请检查网络和被控端");
                if((rule=webSocketChatServer.ruleService.getRuleByAccountAndMac(rule))!=null) {
                    CONNECT_SESSIONS.put(from, rule);
                    if (webSocketChatServer.socketServer.sendMsg(to, Constant.IMAGE, Constant.RESPONSE_SUCCEED,
                            -1, Constant.DF, jsonStr.getBytes(StandardCharsets.UTF_8))) {
                        wsMessage.setContent(JsonUtil.toJsonString(rule));
                    }
                }
                sendMsg(from,wsMessage);
                break;
            case Constant.WS_RES:
                webSocketChatServer.socketServer.sendMsg(to,Constant.RES_UPDATE,Constant.RESPONSE_SUCCEED,
                        -1, Constant.DF,jsonStr.getBytes(StandardCharsets.UTF_8));
                break;
            case Constant.WS_CMD:
                webSocketChatServer.socketServer.sendMsg(to,Constant.CMD,Constant.RESPONSE_SUCCEED,
                        -1, Constant.DF,jsonStr.getBytes(StandardCharsets.UTF_8));
                break;
            default:break;
        }
        log.info("收到消息："+ wsMessage);
    }

    public static void sendMsg(String account,WsMessage wsMessage) throws IOException {
        if(ONLINE_SESSIONS.containsKey(account)){
            ONLINE_SESSIONS.get(account).getBasicRemote().sendText(JsonUtil.toJsonString(wsMessage));
        }
    }

    /**
     * 当关闭连接：1.移除会话对象 2.更新在线人数
     */
    @OnClose
    public void onClose(@PathParam("account") String account) {
        ONLINE_SESSIONS.remove(account);
        if(CONNECT_SESSIONS.containsKey(account)){
            System.out.println(account);
            Rule rule = CONNECT_SESSIONS.get(account);
            webSocketChatServer.socketServer.sendMsg(rule.getMac(),Constant.LOGOUT,
                    Constant.RESPONSE_SUCCEED, -2, Constant.DF,"关闭监控".getBytes());
        }
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
