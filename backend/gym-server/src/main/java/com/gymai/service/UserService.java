package com.gymai.service;

import com.gymai.common.Result;
import com.gymai.entity.User;

public interface UserService {
    Result<User> getProfile(Long userId);
    Result<?> updateProfile(Long userId, User user);
}
