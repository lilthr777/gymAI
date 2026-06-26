package com.gymai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gymai.common.Result;
import com.gymai.config.JwtUtil;
import com.gymai.entity.User;
import com.gymai.mapper.UserMapper;
import com.gymai.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public Result<Map<String, String>> login(String username, String password) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null) {
            return Result.error(401, "用户名或密码错误");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return Result.error(401, "用户名或密码错误");
        }
        if (user.getStatus() == 0) {
            return Result.error(403, "账号已被禁用");
        }
        String token = jwtUtil.generateToken(user.getId(), username, "USER");
        Map<String, String> data = new HashMap<>();
        data.put("token", token);
        data.put("nickname", user.getNickname());
        data.put("avatar", user.getAvatar() != null ? user.getAvatar() : "");
        data.put("userId", String.valueOf(user.getId()));
        return Result.ok(data);
    }

    @Override
    public Result<Map<String, String>> register(String username, String phone, String password) {
        User exist = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (exist != null) {
            return Result.error(400, "用户名已存在");
        }
        User user = new User();
        user.setUsername(username);
        user.setPhone(phone);
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname(username);
        user.setStatus(1);
        userMapper.insert(user);

        String token = jwtUtil.generateToken(user.getId(), username, "USER");
        Map<String, String> data = new HashMap<>();
        data.put("token", token);
        data.put("nickname", user.getNickname());
        data.put("avatar", "");
        data.put("userId", String.valueOf(user.getId()));
        return Result.ok(data);
    }
}
