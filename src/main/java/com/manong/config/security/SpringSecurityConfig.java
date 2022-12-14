package com.manong.config.security;

import com.manong.config.security.fiter.CheckTokenFilter;
import com.manong.config.security.handler.AnonymousAuthenticationHandler;
import com.manong.config.security.handler.CustomerAccessDeniedHandler;
import com.manong.config.security.handler.LoginFailureHandler;
import com.manong.config.security.handler.LoginSuccessHandler;
import com.manong.config.security.service.CustomerUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.parameters.P;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.annotation.Resource;

@Configuration
//@EnableWebFluxSecurity
@EnableWebSecurity
public class SpringSecurityConfig extends WebSecurityConfigurerAdapter {

    @Resource
    private LoginSuccessHandler loginSuccessHandler;

    @Resource
    private LoginFailureHandler loginFailureHandler;
    @Resource
    private AnonymousAuthenticationHandler anonymousAuthenticationHandler;
    @Resource
    private CustomerAccessDeniedHandler customerAccessDeniedHandler;
    @Resource
     private CustomerUserDetailsService customerUserDetailsService;
    @Resource
    private CheckTokenFilter checkTokenFilter;

    /**
     * ???????????????
     * @return
     */
     @Bean
     public BCryptPasswordEncoder passwordEncoder(){
         return  new BCryptPasswordEncoder();
     }

    /**
     * ??????????????????
     * @param http
     * @throws Exception
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //?????????????????????
        http.addFilterBefore(checkTokenFilter, UsernamePasswordAuthenticationFilter.class);

        //??????????????????
        http.formLogin()        //????????????
                .loginProcessingUrl("/api/user/login")//????????????yrl???????????????
                .successHandler(loginSuccessHandler)   //?????????????????????
                .failureHandler(loginFailureHandler)   //?????????????????????
                .usernameParameter("username")
                .and()
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)  ///?????????session
                .and()
                .authorizeRequests()    //???????????????????????????
                .antMatchers("/api/user/login").permitAll() //?????????????????????????????????
                .anyRequest().authenticated() //?????????????????????????????????????????????
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(anonymousAuthenticationHandler)     //?????????????????????
                .accessDeniedHandler(customerAccessDeniedHandler)    //???????????????????????????
                .and()
                .cors();  //??????????????????

    }

    /**
     * ?????????????????????
     * @param auth
     * @throws Exception
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(customerUserDetailsService).passwordEncoder(passwordEncoder());
    }
}
