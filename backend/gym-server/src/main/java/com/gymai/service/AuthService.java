package com.gymai.service;

import com.gymai.common.Result;
import java.util.Map;

public interface AuthService {
    Result<Map<String, String>> login(String username, String password);
    Result<Map<String, String>> register(String username, String phone, String password, String role);
}
