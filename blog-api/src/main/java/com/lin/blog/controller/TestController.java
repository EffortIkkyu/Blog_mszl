package com.lin.blog.controller;

import com.lin.blog.dao.pojo.SysUser;
import com.lin.blog.utils.UserThreadLocal;
import com.lin.blog.vo.Result;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("test")
public class TestController {

    @RequestMapping
    public Result test(){
        SysUser sysUser= UserThreadLocal.get();
        System.out.println(sysUser);
        return Result.success(null);
    }
}