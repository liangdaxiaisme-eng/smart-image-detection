package com.detection.controller;

import com.detection.common.Result;
import com.detection.entity.User;
import com.detection.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Result<?> login(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");
        if (username == null || password == null) {
            return Result.error("用户名和密码不能为空");
        }
        User user = userService.login(username, password);
        if (user == null) {
            return Result.error("用户名或密码错误");
        }
        user.setPassword(null);
        // 简单token：用userId+username
        String token = user.getId() + ":" + user.getUsername();
        return Result.success(Map.of("user", user, "token", token));
    }

    @PostMapping("/register")
    public Result<?> register(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");
        String nickname = params.get("nickname");
        if (username == null || password == null) {
            return Result.error("用户名和密码不能为空");
        }
        User user = userService.register(username, password, nickname);
        if (user == null) {
            return Result.error("用户名已存在");
        }
        user.setPassword(null);
        return Result.success(user);
    }

    @GetMapping("/info")
    public Result<?> info(@RequestHeader("Authorization") String token) {
        try {
            String[] parts = token.split(":");
            Long userId = Long.parseLong(parts[0]);
            User user = userService.getUserById(userId);
            if (user == null) return Result.error(401, "用户不存在");
            user.setPassword(null);
            return Result.success(user);
        } catch (Exception e) {
            return Result.error(401, "Token无效");
        }
    }
}
