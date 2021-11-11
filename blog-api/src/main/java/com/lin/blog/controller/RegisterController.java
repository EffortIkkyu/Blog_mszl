package com.lin.blog.controller;

import com.lin.blog.service.LoginService;
import com.lin.blog.vo.Result;
import com.lin.blog.vo.params.LoginParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("register")
public class RegisterController {

    @Autowired
    private LoginService loginService;

    @PostMapping
    public Result register(@RequestBody LoginParam loginParam){
        //sso，单点登录，后期吧登录注册功能提出去（单独的服务，可以提供接口服务）;
        return loginService.register(loginParam);
    }
}
