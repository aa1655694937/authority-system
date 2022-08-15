package com.manong.config.security.service;

import com.manong.entity.User;
import com.manong.service.UserService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 用户认证处理器类
 */
@Component
public class CustomerUserDetailsService implements UserDetailsService {

    @Resource
    private UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //调用根据用户查询用户信息的方法
        User user = userService.findUserByUserName(username);
        //判断对象是否为空，如果对象为空，则表示登录失败
        if(user==null){
            throw  new UsernameNotFoundException("用户名或者密码错误！");
        }
        //查询成功
        return user;
    }
}
