package com.example.power_prediction.controller;


import com.example.power_prediction.entity.User;
import com.example.power_prediction.service.UserService;
//import org.omg.PortableInterceptor.USER_EXCEPTION;
import com.example.power_prediction.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@CrossOrigin
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    UserService userService;

    @PostMapping("/login")
    public ResponseResult userLogin(@RequestBody User user) {
        return userService.login(user);
    }


    @RequestMapping("/logout")
    public ResponseResult logout() {
        return userService.logout();
    }

    @PostMapping("/register")
    public Integer userRegister(@RequestBody User user) {
        user.setRoot(0);
        return userService.save(user);
    }


}
