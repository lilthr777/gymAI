package com.gymai.controller;

import com.gymai.common.Result;
import com.gymai.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
        return authService.register(body.get("username"), body.get("phone"), body.get("password"));
    }
}
