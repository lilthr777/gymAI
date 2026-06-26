package com.gymai.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gymai.common.Result;
import com.gymai.config.UserPrincipal;
import com.gymai.entity.Checkin;
import com.gymai.service.CheckinService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/checkins")
@RequiredArgsConstructor
public class CheckinController {

    private final CheckinService checkinService;

    @PostMapping
    public Result<?> checkin(@AuthenticationPrincipal UserPrincipal principal,
                             @RequestBody Checkin checkin) {
        return checkinService.checkin(principal.userId(), checkin.getCourseId());
    }

    @GetMapping("/my")
    public Result<Page<Checkin>> myCheckins(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return checkinService.myCheckins(principal.userId(), pageNum, pageSize);
    }
}
