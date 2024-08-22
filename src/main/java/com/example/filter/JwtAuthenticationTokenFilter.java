package com.example.power_prediction.filter;

import com.example.power_prediction.entity.LoginUser;
import com.example.power_prediction.util.JwtUtil;
import com.example.power_prediction.util.RedisCache;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;
import java.util.Set;

@Component
@Slf4j
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    @Autowired
    private RedisCache redisCache;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestURL = request.getRequestURL().toString();
        System.out.println(requestURL);

        //获取token
        String token = request.getHeader("token");
        String timestamp = request.getHeader("timestamp");
        if (!StringUtils.hasText(token)) {
            //放行
            filterChain.doFilter(request, response);
            return;
        }
        //解析token
        String userid;
        try {
            Claims claims = JwtUtil.parseJWT(token);
            userid = claims.getSubject();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("token非法");
        }
        //从redis中获取用户信息
        String redisKey = timestamp + "login:" + userid;
        LoginUser loginUser = redisCache.getCacheObject(redisKey);
        Set<String> strings = redisCache.findByPrex("*login:" + userid);
        if (strings.size() > 1)
            for (String s : strings) {
                if (!s.equals(redisKey)) {
                    redisCache.deleteObject(s);
                }
            }

        loginUser.getUser().setPhoneNumber(timestamp);
        if (Objects.isNull(loginUser)) {
            throw new RuntimeException("用户未登录");
        }
        //存入SecurityContextHolder
        //TODO 获取权限信息封装到Authentication中
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginUser, null, null);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        //放行

        filterChain.doFilter(request, response);
    }
}
