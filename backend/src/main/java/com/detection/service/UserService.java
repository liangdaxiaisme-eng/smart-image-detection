package com.detection.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.detection.entity.User;
import com.detection.mapper.UserMapper;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserService extends ServiceImpl<UserMapper, User> {

    public User login(String username, String password) {
        LambdaQueryWrapper<User> q = new LambdaQueryWrapper<>();
        q.eq(User::getUsername, username).eq(User::getPassword, password);
        return getOne(q);
    }

    public User register(String username, String password, String nickname) {
        // 检查是否已存在
        LambdaQueryWrapper<User> q = new LambdaQueryWrapper<>();
        q.eq(User::getUsername, username);
        if (getOne(q) != null) return null;

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setNickname(nickname != null ? nickname : username);
        user.setRole("user");
        save(user);
        return user;
    }

    public User getUserById(Long id) {
        return getById(id);
    }

    public List<User> getAllUsers() {
        return list();
    }
}
