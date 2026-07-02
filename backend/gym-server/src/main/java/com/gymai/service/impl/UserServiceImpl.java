package com.gymai.service.impl;

import com.gymai.common.Result;
import com.gymai.entity.User;
import com.gymai.mapper.UserMapper;
import com.gymai.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    @Override
    public Result<User> getProfile(Long userId) {
        return Result.ok(userMapper.selectById(userId));
    }

    @Override
    public Result<?> updateProfile(Long userId, User user) {
        user.setId(userId);
        user.setPassword(null);
        user.setUsername(null);
        userMapper.updateById(user);
        return Result.ok();
    }
}
