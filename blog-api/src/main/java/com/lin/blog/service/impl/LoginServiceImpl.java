package com.lin.blog.service.impl;

import com.alibaba.fastjson.JSON;
import com.lin.blog.dao.pojo.SysUser;
import com.lin.blog.service.LoginService;
import com.lin.blog.service.SysUserService;
import com.lin.blog.utils.JWTUtils;
import com.lin.blog.vo.ErrorCode;
import com.lin.blog.vo.Result;
import com.lin.blog.vo.params.LoginParam;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
public class LoginServiceImpl implements LoginService {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    private static final String slat = "ikkyu!@#";

    @Override
    public Result login(LoginParam loginParam) {
        /**
         * 检查参数是否合法
         * 根据用户名密码去user表中查询是否存在
         * 如果不存在，那么就登录，存在.使用jwt生成token，返回前端
         * token放入redis当中，redis token：user信息 设置过期时间
         * （登录认证，先认证token字符串，去redis认证，是否存在）
         */
        String account = loginParam.getAccount();
        String password = loginParam.getPassword();
        if (StringUtils.isBlank(account)||StringUtils.isBlank(password))
        {
            return Result.fail(ErrorCode.PARAMS_ERROR.getCode(),ErrorCode.PARAMS_ERROR.getMsg());
        }
        password  = DigestUtils.md5Hex(password+slat);
        SysUser sysUser=sysUserService.findUser(account,password);
        if (sysUser==null)
        {
            return Result.fail(ErrorCode.ACCOUNT_PWD_NOT_EXIST.getCode(),ErrorCode.ACCOUNT_PWD_NOT_EXIST.getMsg());
        }

        String token = JWTUtils.createToken(sysUser.getId());
        redisTemplate.opsForValue().set("TOKEN_"+token, JSON.toJSONString(sysUser),1, TimeUnit.DAYS);


        return Result.success(token);
    }

    @Override
    public SysUser checkToken(String token) {
        if (StringUtils.isBlank(token))
        {
            return null;
        }
        Map<String, Object> stringObjectMap = JWTUtils.checkToken(token);
        if (stringObjectMap==null)
        {
            return null;
        }
        String userJson = redisTemplate.opsForValue().get("TOKEN_" + token);
        if (StringUtils.isBlank(userJson))
        {
            return null;
        }
        SysUser sysUser = JSON.parseObject(userJson, SysUser.class);

        return sysUser;
    }

    @Override
    public Result logout(String token) {
        redisTemplate.delete("TOKEN_"+token);
        return Result.success(null);
    }

    @Override
    public Result register(LoginParam loginParam) {
        /**
         * 判断参数是否合法
         * 判断账户是否存在。存在。返回账号已经被注册
         * 账户不存在，注册用户
         * 生成token
         * 传入redis，并返回
         * 加上事务，一旦中间任何过程出现问题，注册的用户立刻回滚
         */
        String account = loginParam.getAccount();
        String password = loginParam.getPassword();
        String nickname = loginParam.getNickname();
        if (StringUtils.isBlank(account)
                || StringUtils.isBlank(password)
                || StringUtils.isBlank(nickname)
        ){
            return Result.fail(ErrorCode.PARAMS_ERROR.getCode(),ErrorCode.PARAMS_ERROR.getMsg());
        }
        SysUser sysUser=sysUserService.findUserByAccount(account);
        if (sysUser!=null)
        {
            return Result.fail(ErrorCode.ACCOUNT_EXIST.getCode(),"账号已经被注册了");
        }
        sysUser=new SysUser();
        sysUser.setNickname(nickname);
        sysUser.setAccount(account);
        sysUser.setPassword(DigestUtils.md5Hex(password+slat));
        sysUser.setCreateDate(System.currentTimeMillis());
        sysUser.setLastLogin(System.currentTimeMillis());
        sysUser.setAvatar("/static/img/logo.b3a48c0.png");
        sysUser.setAdmin(1); //1 为true
        sysUser.setDeleted(0); // 0 为false
        sysUser.setSalt("");
        sysUser.setStatus("");
        sysUser.setEmail("");
        this.sysUserService.save(sysUser);

        String token = JWTUtils.createToken(sysUser.getId());
        redisTemplate.opsForValue().set("TOKEN_"+token, JSON.toJSONString(sysUser),1, TimeUnit.DAYS);


        return Result.success(token);

    }
}
