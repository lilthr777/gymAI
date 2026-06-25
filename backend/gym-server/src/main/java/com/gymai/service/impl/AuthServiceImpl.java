package com.gymai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gymai.common.Result;
import com.gymai.config.JwtUtil;
import com.gymai.entity.Admin;
import com.gymai.mapper.AdminMapper;
import com.gymai.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AdminMapper adminMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public Result<Map<String, String>> login(String username, String password) {
        Admin admin = adminMapper.selectOne(
                new LambdaQueryWrapper<Admin>().eq(Admin::getUsername, username));
        if (admin == null) {
            return Result.error(401, "用户名或密码错误");
        }
        if (!passwordEncoder.matches(password, admin.getPassword())) {
            return Result.error(401, "用户名或密码错误");
        }
        String token = jwtUtil.generateToken(username);
        Map<String, String> data = new HashMap<>();
        data.put("token", token);
        data.put("nickname", admin.getNickname());
        data.put("avatar", admin.getAvatar());
        return Result.ok(data);
    }
}
