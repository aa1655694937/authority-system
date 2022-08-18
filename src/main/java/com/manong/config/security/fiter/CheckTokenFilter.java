package com.manong.config.security.fiter;

import com.manong.config.redis.RedisService;
import com.manong.config.security.exception.CustomerAuthenticationException;
import com.manong.config.security.handler.LoginFailureHandler;
import com.manong.config.security.service.CustomerUserDetailsService;
import com.manong.utils.JwtUtils;
import io.netty.util.internal.ObjectUtil;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Data
@Component
public class CheckTokenFilter extends OncePerRequestFilter {
    //登录请求地址
    @Value("${request.login.url}")
    private String loginurl;

    @Resource
    private RedisService redisService;

    @Resource
    private JwtUtils jwtUtils;

    @Resource
    private CustomerUserDetailsService customerUserDetailsService;

    @Resource
    private LoginFailureHandler loginFailureHandler;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            //获取当前请求的url地址
            String url = request.getRequestURI();
            //判断当前请求是否是登录请求，如果不是登录请求，则需要验证token
            if (!url.equals(loginurl)){
                //进行token验证
                this.validateToken(request);
            }
        } catch (AuthenticationException e) {
            //验证失败
            loginFailureHandler.onAuthenticationFailure(request,response,e);
        }
        //登录请求不需要携带token，可以直接放行
        doFilter(request,response,filterChain);
    }

    /**
     * 验证token信息
     * @param request
     */
    private void validateToken(HttpServletRequest request) {
        //获取前端提交token信息
        //从headers头部获取token信息
        String token = request.getHeader("token");
        //如果请求头部没有携带token，则从请求的参数中获取token
        if (ObjectUtils.isEmpty(token)){
            token = request.getParameter("token");//从参数中获取
        }
        //如果请求参数中也没有携带token信息，则抛出异常
        if (ObjectUtils.isEmpty(token)){
            throw new CustomerAuthenticationException("token不存在");
        }
        //判断redis中是否存在token信息
        String tokenKey = "token_" + token;
        String redisToken = redisService.get(tokenKey);
        //判断Redis中是否存在token信息，如果为空，则表示token已经失效
        if (ObjectUtils.isEmpty(redisToken)){
            throw new CustomerAuthenticationException("token已过期");
        }
        //如果token和Redis中的token不一致，则验证失败
        if (!token.equals(redisToken)){
            throw new CustomerAuthenticationException("token验证失败");

        }
        //如果token是存在，则从token中解析出用户名
        String username = jwtUtils.getUsernameFromToken(token);
        //判断用户名是否为空
        if (ObjectUtils.isEmpty(username)){
            throw new CustomerAuthenticationException("token解析失败");

        }
        //获取用户信息
        UserDetails userDetails = customerUserDetailsService.loadUserByUsername(username);
        //判断用户信息是否为空
        if (userDetails == null){
            throw new CustomerAuthenticationException("token验证失败");
        }
        //创建用户身份认证对象
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails,null,userDetails.getAuthorities());
        //设置请求信息
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        //将验证的信息交给Spring Security 上下文
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

    }
}
