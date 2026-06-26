package com.gymai.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gymai.common.Result;
import com.gymai.config.UserPrincipal;
import com.gymai.entity.Course;
import com.gymai.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @GetMapping
    public Result<Page<Course>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long coachId) {
        return courseService.page(pageNum, pageSize, keyword, coachId);
    }

    @GetMapping("/{id}")
    public Result<Course> getById(@PathVariable Long id) {
        return courseService.getById(id);
    }

    @PostMapping("/{id}/register")
    public Result<?> register(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        return courseService.register(principal.userId(), id);
    }

    @PostMapping("/{id}/cancel")
    public Result<?> cancel(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        return courseService.cancel(principal.userId(), id);
    }

    @GetMapping("/my")
    public Result<Page<Course>> myCourses(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return courseService.myCourses(principal.userId(), pageNum, pageSize);
    }
}
