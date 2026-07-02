package com.gymai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gymai.common.Result;
import com.gymai.config.JwtUtil;
import com.gymai.entity.Coach;
import com.gymai.entity.User;
import com.gymai.mapper.CoachMapper;
import com.gymai.mapper.UserMapper;
import com.gymai.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final CoachMapper coachMapper;
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
        // 使用数据库中的真实角色生成 JWT
        String role = user.getRole() != null ? user.getRole() : "MEMBER";
        String token = jwtUtil.generateToken(user.getId(), username, role);
        Map<String, String> data = new HashMap<>();
        data.put("token", token);
        data.put("nickname", user.getNickname());
        data.put("avatar", user.getAvatar() != null ? user.getAvatar() : "");
        data.put("userId", String.valueOf(user.getId()));
        data.put("role", role);
        return Result.ok(data);
    }

    @Override
    @Transactional
    public Result<Map<String, String>> register(String username, String phone, String password, String role) {
        User exist = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (exist != null) {
            return Result.error(400, "用户名已存在");
        }
        // 只允许注册为 MEMBER、COACH 或 ADMIN
        if (role == null || role.isEmpty()) {
            role = "MEMBER";
        }
        role = role.toUpperCase();
        if (!role.equals("MEMBER") && !role.equals("COACH") && !role.equals("ADMIN")) {
            role = "MEMBER";
        }

        User user = new User();
        user.setUsername(username);
        user.setPhone(phone);
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname(username);
        user.setRole(role);
        userMapper.insert(user);

        // 教练注册时自动创建 coach 表记录
        if ("COACH".equals(role)) {
            Coach existingCoach = coachMapper.selectOne(
                    new LambdaQueryWrapper<Coach>().eq(Coach::getPhone, phone));
            if (existingCoach == null) {
                Coach coach = new Coach();
                coach.setName(username);
                coach.setPhone(phone);
                coach.setGender(0);
                coach.setStatus(1);
                coachMapper.insert(coach);
            }
        }

        String token = jwtUtil.generateToken(user.getId(), username, role);
        Map<String, String> data = new HashMap<>();
        data.put("token", token);
        data.put("nickname", user.getNickname());
        data.put("avatar", "");
        data.put("userId", String.valueOf(user.getId()));
        data.put("role", role);
        return Result.ok(data);
    }
}
