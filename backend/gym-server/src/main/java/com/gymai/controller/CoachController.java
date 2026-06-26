package com.gymai.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gymai.common.Result;
import com.gymai.entity.Coach;
import com.gymai.service.CoachService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/coaches")
@RequiredArgsConstructor
public class CoachController {

    private final CoachService coachService;

    @GetMapping
    public Result<Page<Coach>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        return coachService.page(pageNum, pageSize, keyword);
    }

    @GetMapping("/{id}")
    public Result<Coach> getById(@PathVariable Long id) {
        return coachService.getById(id);
    }
}
