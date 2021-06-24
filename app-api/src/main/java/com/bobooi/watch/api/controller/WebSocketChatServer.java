package com.bobooi.watch.api.controller;

import com.bobooi.watch.common.utils.JsonUtil;
import com.bobooi.watch.common.utils.misc.Constant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author bobo
 * @date 2021/6/22
 */

@Slf4j
@Component
@ServerEndpoint("/{userId}")//标记此类为服务端
public class WebSocketChatServer {

    private static Map<String, Session> onlineSessions = new ConcurrentHashMap<>();

    public static List<String> getOnlineSessions(){
        List<String> users = new ArrayList<>(onlineSessions.size());
        onlineSessions.forEach((userId, session) -> users.add(userId));
        return users;
    }

    public void userChange(String userId, String type){
        Message message = new Message(userId,"",type,JsonUtil.toJsonString(getOnlineSessions()));
        onlineSessions.forEach(((id, session1) -> {
            try {
                session1.getBasicRemote().sendText(JsonUtil.toJsonString(message));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));
    }


    /**
     * 当客户端打开连接：1.添加会话对象 2.更新在线人数
     */
    @OnOpen
    public void onOpen(Session session,@PathParam("userId") String userId) {
        onlineSessions.put(userId, session);
        userChange(userId, Constant.OPEN);
        System.out.println("成功连接，在线人数："+onlineSessions.size());
    }

    /**
     * 当客户端发送消息：1.获取它的用户名和消息 2.发送消息给所有人
     * <p>
     * PS: 这里约定传递的消息为JSON字符串 方便传递更多参数！
     */
    @OnMessage
    public void onMessage(String jsonStr) throws IOException {
        System.out.println(jsonStr);
        Message message = JsonUtil.parseObject(jsonStr, Message.class);
        if(Constant.OPEN.equals(message.type)){

        }else{
            if(onlineSessions.containsKey(message.toUserId)){
                onlineSessions.get(message.toUserId).getBasicRemote().sendText(JsonUtil.toJsonString(message));
            }
        }
        log.info("收到消息："+message);
    }

    /**
     * 当关闭连接：1.移除会话对象 2.更新在线人数
     */
    @OnClose
    public void onClose(@PathParam("userId") String userId) {
        onlineSessions.remove(userId);
        userChange(userId, Constant.CLOSE);
        System.out.println("关闭连接，在线人数："+onlineSessions.size());
    }

    /**
     * 当通信发生异常：打印错误日志
     */
    @OnError
    public void onError(Session session, Throwable error) {
        error.printStackTrace();
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Message{
        private String fromUserId;
        private String toUserId;
        private String type;
        private String content;
    }

}
