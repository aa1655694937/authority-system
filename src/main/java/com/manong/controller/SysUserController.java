package com.manong.controller;

import com.manong.config.redis.RedisService;
import com.manong.utils.JwtUtils;
import com.manong.utils.Result;
import com.manong.vo.TokenVo;
import io.jsonwebtoken.Jwts;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/sysUser")
public class SysUserController {

    @Resource
    private JwtUtils jwtUtils;

    @Resource
    private RedisService redisService;
    /**
     * 刷新token
     * @param request
     * @return
     */
    @PostMapping("/refreshToken")
    public Result refreshToken(HttpServletRequest request){
        //从headers中获取token信息
        String token = request.getHeader("token");
        //判断headers头部是否存在token信息
        if (ObjectUtils.isEmpty(token)){
            //从请求中获取token
            token = request.getParameter("token");

        }
        //从spring Sectiy 上下文获取拥挤西欧毛线哦
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        //获取用户身份信息
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        //定义变量保存信息token信息
        String newToken = "";
        //验证提交过来的token信息是否是合法的
        if (jwtUtils.validateToken(token,userDetails)){
            //从新生成新的token
            newToken = jwtUtils.refreshToken(token);
        }
        //获取本次token的到期时间
        long expirTime = Jwts.parser()
                .setSigningKey(jwtUtils.getSecret())
                .parseClaimsJws(newToken.replace("jwt_", ""))
                .getBody().getExpiration().getTime();
        //清除原来的token信息
        String oldTokenKey = "token_" + token;
        redisService.del(oldTokenKey);
        //将新的token信息保存到缓存中
        String newTokenKey = "token_" + newToken;
        redisService.set(newTokenKey,newToken, jwtUtils.getExpiration()/1000);
        //创建TokenVo对象
        TokenVo tokenVo = new TokenVo(expirTime,newToken);
        //返回数据
        return  Result.ok(tokenVo).message("token刷新成功");

    }
}
