package com.gymai.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gymai.common.Result;
import com.gymai.entity.Checkin;
import com.gymai.service.CheckinService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/checkins")
@RequiredArgsConstructor
public class CheckinController {

    private final CheckinService checkinService;

    @GetMapping
    public Result<Page<Checkin>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) Long courseId) {
        return checkinService.page(pageNum, pageSize, memberId, courseId);
    }

    @PostMapping
    public Result<?> checkin(@RequestBody Checkin checkin) {
        return checkinService.checkin(checkin);
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        return checkinService.stats();
    }
}
