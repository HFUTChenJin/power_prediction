package com.example.power_prediction.service.Impl;

import com.example.power_prediction.entity.LoginUser;
import com.example.power_prediction.entity.User;
import com.example.power_prediction.repository.UserRepository;
import com.example.power_prediction.service.UserService;
import com.example.power_prediction.util.JwtUtil;
import com.example.power_prediction.util.RedisCache;
import com.example.power_prediction.util.ResponseResult;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class ImplUserService implements UserService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    RedisCache redisCache;

    @Override
    public ResponseResult login(User user) {
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword());
        Authentication authenticate = authenticationManager.authenticate(usernamePasswordAuthenticationToken);
        if (Objects.isNull(authenticate)) {
            throw new RuntimeException("登录失败");
        }

        LoginUser loginUser = (LoginUser) authenticate.getPrincipal();


        String userId = loginUser.getUser().getUserId().toString();
        String jwt = JwtUtil.createJWT(userId);
        Map map = new HashMap();
        map.put("token", jwt);
        map.put("role", loginUser.getUser().getRoot());
        map.put("timestamp", System.currentTimeMillis() / 1000);
        redisCache.setCacheObject(System.currentTimeMillis() / 1000 + "login:" + userId, loginUser);
        return new ResponseResult(200, "登录成功", map);
//        Map<String, Object> map = new HashMap<>();
//        User newUser = userRepository.findByUsernameAndPassword(user.getUsername(), user.getPassword());
//        if (newUser != null) {
//            map.put("username", newUser.getUsername());
//            map.put("root", newUser.getRoot());
//            map.put("state", "Success");
//            map.put("userDepartment,", newUser.getDepartment());
//            System.out.println("Success");
//        } else {
//            map.put("state", "Fail");
//            System.out.println("Fail");
//        }
//        return map;
    }

    @Override
    public Integer save(User user) {
        try {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            userRepository.save(user);
            return 1;
        } catch (Exception e) {
            return -1;
        }
    }

    @Override
    public ResponseResult logout() {
        UsernamePasswordAuthenticationToken authentication = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        Integer userId = loginUser.getUser().getUserId();
        String timestamp = loginUser.getUser().getPhoneNumber();
        //删除redis中的值
        redisCache.deleteObject(timestamp + "login:" + userId);
        return new ResponseResult(200, "注销成功");
    }
}
