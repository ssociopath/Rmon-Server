package com.bobooi.watch.common.component;

import com.bobooi.watch.common.utils.JsonUtil;
import com.bobooi.watch.common.utils.misc.Constant;
import lombok.Data;
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
@ServerEndpoint("/chat/{userId}")//标记此类为服务端
public class WebSocketChatServer {

    private static Map<String, Session> onlineSessions = new ConcurrentHashMap<>();

    public static List<String> getOnlineSessions(){
        List<String> users = new ArrayList<>(onlineSessions.size());
        onlineSessions.forEach((userId, session) -> users.add(userId));
        return users;
    }


    /**
     * 当客户端打开连接：1.添加会话对象 2.更新在线人数
     */
    @OnOpen
    public void onOpen(Session session,@PathParam("userId") String userId) {
        onlineSessions.put(userId, session);
        System.out.println("成功连接，在线人数："+onlineSessions.size());
    }

    /**
     * 当客户端发送消息：1.获取它的用户名和消息 2.发送消息给所有人
     * <p>
     * PS: 这里约定传递的消息为JSON字符串 方便传递更多参数！
     */
    @OnMessage
    public void onMessage(Session session, String jsonStr) throws IOException {
        Message message = JsonUtil.parseObject(jsonStr, Message.class);
        if(Constant.OPEN.equals(message.type)){

        }else{
            onlineSessions.get(message.toUserId).getBasicRemote().sendText(JsonUtil.toJsonString(message));
        }
        log.info("收到消息："+message);
    }

    /**
     * 当关闭连接：1.移除会话对象 2.更新在线人数
     */
    @OnClose
    public void onClose(Session session,@PathParam("userId") String userId) {
        onlineSessions.remove(userId);
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
    public static class Message{
        private String fromUserId;
        private String toUserId;
        private String type;
        private String content;
    }

}
