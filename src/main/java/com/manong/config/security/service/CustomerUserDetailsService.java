package com.manong.config.security.service;

import com.manong.entity.Permission;
import com.manong.entity.User;
import com.manong.service.PermissionService;
import com.manong.service.UserService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 用户认证处理器类
 */
@Component
public class CustomerUserDetailsService implements UserDetailsService {

    @Resource
    private UserService userService;
    @Resource
    private PermissionService permissionService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //调用根据用户查询用户信息的方法
        User user = userService.findUserByUserName(username);
        //判断对象是否为空，如果对象为空，则表示登录失败
        if(user==null){
            throw  new UsernameNotFoundException("用户名或者密码错误！");
        }
        //查询当前登录用户拥有的权限列表
        List<Permission> permissionList = permissionService.findPermissionListByUserId(user.getId());
        //  获取对应的权限编码
        List<String> codeList = permissionList.stream()
                .filter(Objects::nonNull)
                .map(item -> item.getCode())
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        //将权限编码列表转换数组
        String [] strings = codeList.toArray(new String[codeList.size()]);
        //设置权限列表
        List<GrantedAuthority> authorityList = AuthorityUtils.createAuthorityList(strings);
        //将权限列表设置给user对象
        user.setAuthorities(authorityList);
        //设置该用户用户的菜单信息
        user.setPermissionList(permissionList);
        //查询成功
        return user;
    }
}
