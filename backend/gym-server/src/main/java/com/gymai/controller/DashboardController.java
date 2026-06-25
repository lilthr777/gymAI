package com.gymai.controller;

import com.gymai.common.Result;
import com.gymai.mapper.CheckinMapper;
import com.gymai.mapper.CoachMapper;
import com.gymai.mapper.CourseMapper;
import com.gymai.mapper.MemberMapper;
import com.gymai.entity.Checkin;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final MemberMapper memberMapper;
    private final CoachMapper coachMapper;
    private final CourseMapper courseMapper;
    private final CheckinMapper checkinMapper;

    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        Map<String, Object> data = new HashMap<>();
        data.put("totalMembers", memberMapper.selectCount(null));
        data.put("totalCoaches", coachMapper.selectCount(null));
        data.put("totalCourses", courseMapper.selectCount(null));
        data.put("todayCheckins", checkinMapper.selectCount(
                new LambdaQueryWrapper<Checkin>()
                        .ge(Checkin::getCheckinTime, LocalDate.now().atStartOfDay())));
        return Result.ok(data);
    }

    @GetMapping("/member-growth")
    public Result<List<Map<String, Object>>> memberGrowth() {
        List<Map<String, Object>> list = new ArrayList<>();
        String[] months = {"1月", "2月", "3月", "4月", "5月", "6月"};
        int[] values = {120, 135, 148, 162, 175, 190};
        for (int i = 0; i < months.length; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("month", months[i]);
            item.put("count", values[i]);
            list.add(item);
        }
        return Result.ok(list);
    }

    @GetMapping("/course-type-distribution")
    public Result<List<Map<String, Object>>> courseTypeDistribution() {
        List<Map<String, Object>> list = new ArrayList<>();
        String[][] types = {{"力量训练", "35"}, {"瑜伽", "25"}, {"有氧运动", "20"}, {"搏击", "12"}, {"其他", "8"}};
        for (String[] type : types) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", type[0]);
            item.put("value", Integer.parseInt(type[1]));
            list.add(item);
        }
        return Result.ok(list);
    }

    @GetMapping("/checkin-weekly")
    public Result<List<Map<String, Object>>> checkinWeekly() {
        List<Map<String, Object>> list = new ArrayList<>();
        String[] days = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
        int[] values = {45, 52, 38, 60, 55, 80, 72};
        for (int i = 0; i < days.length; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("day", days[i]);
            item.put("count", values[i]);
            list.add(item);
        }
        return Result.ok(list);
    }
}
