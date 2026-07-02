package com.gymai.controller;

import com.gymai.common.Result;
import com.gymai.config.UserPrincipal;
import com.gymai.dto.AdminDashboardDTO;
import com.gymai.dto.CoachScheduleDTO;
import com.gymai.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/coach")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('COACH', 'ADMIN')")
public class CoachBackendController {

    private final AdminService adminService;

    /** 教练仪表盘 */
    @GetMapping("/dashboard")
    public Result<AdminDashboardDTO> dashboard(@AuthenticationPrincipal UserPrincipal principal) {
        return adminService.coachDashboard(principal.userId());
    }

    /** 教练日程：查看自己的课程和报名会员信息 */
    @GetMapping("/schedule")
    public Result<List<CoachScheduleDTO>> schedule(@AuthenticationPrincipal UserPrincipal principal) {
        return adminService.coachSchedule(principal.userId());
    }
}
