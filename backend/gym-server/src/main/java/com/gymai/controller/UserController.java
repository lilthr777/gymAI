package com.gymai.controller;

import com.gymai.common.Result;
import com.gymai.config.UserPrincipal;
import com.gymai.entity.User;
import com.gymai.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public Result<User> profile(@AuthenticationPrincipal UserPrincipal principal) {
        return userService.getProfile(principal.userId());
    }

    @PutMapping("/profile")
    public Result<?> updateProfile(@AuthenticationPrincipal UserPrincipal principal, @RequestBody User user) {
        return userService.updateProfile(principal.userId(), user);
    }
}
