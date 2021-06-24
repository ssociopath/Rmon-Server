package com.bobooi.watch.api.controller;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;


/**
 * @author bobo
 * @date 2021/6/22
 */

@CrossOrigin(allowCredentials = "true")
@Controller
public class MessageController {
    @RequestMapping("/index")
    public String index(){
        return "index";
    }
}
