package com.gymai.controller;

import com.gymai.common.Result;
import com.gymai.config.UserPrincipal;
import com.gymai.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Result<Map<String, String>> login(@RequestBody Map<String, String> body) {
        return authService.login(body.get("username"), body.get("password"));
    }

    @PostMapping("/register")
    public Result<Map<String, String>> register(@RequestBody Map<String, String> body) {
        String role = body.getOrDefault("role", "MEMBER");
        return authService.register(body.get("username"), body.get("phone"), body.get("password"), role);
    }

    /** 获取当前登录用户信息（含角色） */
    @GetMapping("/me")
    public Result<Map<String, Object>> me(@AuthenticationPrincipal UserPrincipal principal) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", principal.userId());
        data.put("username", principal.username());
        data.put("role", principal.role());
        return Result.ok(data);
    }
}
