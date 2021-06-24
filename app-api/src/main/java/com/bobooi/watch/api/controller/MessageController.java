package com.bobooi.watch.api.controller;

import com.bobooi.watch.common.component.WebSocketChatServer;
import com.bobooi.watch.common.response.ApplicationResponse;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author bobo
 * @date 2021/6/22
 */

@CrossOrigin(allowCredentials = "true")
@RestController
@RequestMapping("/msg")
public class MessageController {
    @GetMapping
    public ApplicationResponse<List<String>> test(){
        return ApplicationResponse.succeed(WebSocketChatServer.getOnlineSessions());
    }
}
