package com.example.power_prediction.config;

import com.example.power_prediction.filter.JwtAuthenticationTokenFilter;
import com.example.power_prediction.security.CustomizeAuthenticationEntryPoint;
import com.example.power_prediction.security.ImplUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    //        //登录成功处理逻辑
//        @Autowired
//        CustomizeAuthenticationSuccessHandler authenticationSuccessHandler;
//
//        //登录失败处理逻辑
//        @Autowired
//        CustomizeAuthenticationFailureHandler authenticationFailureHandler;
//
//        //权限拒绝处理逻辑
//        @Autowired
//        CustomizeAccessDeniedHandler accessDeniedHandler;
//
//匿名用户访问无权限资源时的异常
    @Autowired
    private JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;
    @Autowired
    CustomizeAuthenticationEntryPoint authenticationEntryPoint;

    //
//        //会话失效(账号被挤下线)处理逻辑
//        @Autowired
//        CustomizeSessionInformationExpiredStrategy sessionInformationExpiredStrategy;
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        //获取用户账号密码及权限信息
        return new ImplUserDetailsService();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        // 设置默认的加密方式（强hash方式加密）
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService());
    }

    @Override
    public void configure(WebSecurity web) {
        // 可以直接访问的静态数据
        web.ignoring()
                .antMatchers("/static/css/**")
                .antMatchers("/static/index.html")
                .antMatchers("/static/img/**")
                .antMatchers("/")
                .antMatchers("/static/fonts/**")
                .antMatchers("/static/js/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors().and().csrf().disable()
                //关闭csrf
                //不通过Session获取SecurityContext
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                // 对于登录接口 允许匿名访问
                .antMatchers("/user/login").anonymous()
                .antMatchers("/**").anonymous()
//                .antMatchers("/testCors").hasAuthority("system:dept:list222")
                // 除上面外的所有请求全部需要鉴权认证
                .anyRequest().authenticated()
                .and().exceptionHandling().authenticationEntryPoint(authenticationEntryPoint);

        http.addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);
    }

}