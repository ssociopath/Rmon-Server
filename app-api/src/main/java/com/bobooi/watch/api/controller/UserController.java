package com.bobooi.watch.api.controller;
import com.bobooi.watch.common.response.ApplicationResponse;
import com.bobooi.watch.data.entity.User;
import com.bobooi.watch.data.service.concrete.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;


/**
 * @author bobo
 * @date 2021/6/22
 */

@CrossOrigin(allowCredentials = "true")
@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    UserService userService;

    @PostMapping("/login")
    public ApplicationResponse<User> login(@Validated User user){
        return ApplicationResponse.succeed(userService.getUserByAccountAndPwd(user));
    }

    @PostMapping("/register")
    public ApplicationResponse<Void> register(@Validated User user){
        userService.addUser(user);
        return ApplicationResponse.succeed();
    }
}
