package com.gymai.service;

import com.gymai.common.Result;
import java.util.Map;

public interface AuthService {
    Result<Map<String, String>> login(String username, String password);
}
