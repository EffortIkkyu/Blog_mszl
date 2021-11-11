package com.lin.blog.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Result {
    //  是否成功
    private boolean success;
    //编码
    private int code;
    //信息
    private String msg;
    //数据
    private Object data;

    public static Result success(Object data){
        return new Result(true,200,"success",data);
    }
    public static Result fail(int code,String msg)
    {
        return new Result(false,code,msg,null);
    }
}
