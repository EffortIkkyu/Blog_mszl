package com.lin.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lin.blog.dao.mapper.SysUserMapper;
import com.lin.blog.dao.pojo.SysUser;
import com.lin.blog.service.LoginService;
import com.lin.blog.service.SysUserService;
import com.lin.blog.vo.ErrorCode;
import com.lin.blog.vo.LoginUserVo;
import com.lin.blog.vo.Result;
import com.lin.blog.vo.UserVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class SysUserServiceImpl implements SysUserService {
    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @Autowired
    private LoginService loginService;

//    @Override
//    public UserVo findUserVoById(Long id) {
//        SysUser sysUser = sysUserMapper.selectById(id);
//        if (sysUser==null)
//        {
//            sysUser=new SysUser();
//            sysUser.setId(1L);
//            sysUser.setAvatar("/static/img/logo.b3a48c0.png");
//            sysUser.setNickname("ikkyu");
//        }
//        UserVo userVo=new UserVo();
//        BeanUtils.copyProperties(sysUser,userVo);
//        return userVo;
//    }
    @Override
    public UserVo findUserVoById(Long id) {
        SysUser sysUser = sysUserMapper.selectById(id);
        if (sysUser == null){
            sysUser = new SysUser();
            sysUser.setId(1L);
            sysUser.setAvatar("/static/img/logo.b3a48c0.png");
            sysUser.setNickname("ikkyu");
        }
        UserVo userVo = new UserVo();
        BeanUtils.copyProperties(sysUser,userVo);
        userVo.setId(String.valueOf(sysUser.getId()));
//        userVo.setAvatar(sysUser.getAvatar());
//        userVo.setNickname(sysUser.getNickname());
//        userVo.setId(sysUser.getId());
        return userVo;
    }
    @Override
    public SysUser findUserById(Long id) {
        SysUser sysUser = sysUserMapper.selectById(id);
        if (sysUser==null)
        {
            sysUser=new SysUser();
            sysUser.setNickname("ikkyu");
        }
        return sysUser;
    }

    @Override
    public SysUser findUser(String account, String password) {
        LambdaQueryWrapper<SysUser> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getAccount,account);
        queryWrapper.eq(SysUser::getPassword,password);
        queryWrapper.select(SysUser::getAccount,SysUser::getId,SysUser::getAvatar,SysUser::getNickname);
        queryWrapper.last("limit 1");

        return sysUserMapper.selectOne(queryWrapper);
    }

    @Override
    public Result findUserByToken(String token) {
        /**
         * 1.token合法性校验
         * 是否为空,解析是否成功，redis是否存在
         * 如果校验失败，返回错误
         * 如果成功，返回对应结果，loginUserVo
         */

        SysUser sysUser=loginService.checkToken(token);
        if (sysUser==null)
        {
            Result.fail(ErrorCode.TOKEN_ERROR.getCode(),ErrorCode.TOKEN_ERROR.getMsg());
        }
        LoginUserVo loginUserVo=new LoginUserVo();
        loginUserVo.setId(String.valueOf(sysUser.getId()));
        loginUserVo.setNickname(sysUser.getNickname());
        loginUserVo.setAvatar(sysUser.getAvatar());
        loginUserVo.setAccount(sysUser.getAccount());

        return Result.success(loginUserVo);
    }

    @Override
    public SysUser findUserByAccount(String account) {
        LambdaQueryWrapper<SysUser> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getAccount,account);
        queryWrapper.last("limit 1");
        return this.sysUserMapper.selectOne(queryWrapper);
    }

    @Override
    public void save(SysUser sysUser) {
        //保存用户。id自动生成
        //默认生成的id 是分布式id 雪花算法
        this.sysUserMapper.insert(sysUser);
    }
}
