package com.gymai.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gymai.common.Result;
import com.gymai.config.UserPrincipal;
import com.gymai.entity.*;
import com.gymai.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
public class HomeController {

    private final UserCourseMapper userCourseMapper;
    private final CheckinMapper checkinMapper;
    private final CourseMapper courseMapper;
    private final CoachMapper coachMapper;

    @GetMapping
    public Result<Map<String, Object>> home(@AuthenticationPrincipal UserPrincipal principal) {
        Long userId = principal.userId();
        Map<String, Object> data = new HashMap<>();

        // 我的课程数
        long myCourseCount = userCourseMapper.selectCount(
                new LambdaQueryWrapper<UserCourse>().eq(UserCourse::getUserId, userId));
        data.put("myCourseCount", myCourseCount);

        // 本月签到次数
        long monthCheckins = checkinMapper.selectCount(
                new LambdaQueryWrapper<Checkin>()
                        .eq(Checkin::getUserId, userId)
                        .ge(Checkin::getCheckinTime, LocalDate.now().withDayOfMonth(1).atStartOfDay()));
        data.put("monthCheckins", monthCheckins);

        // 推荐课程（最近的5条）
        Page<Course> coursePage = new Page<>(1, 5);
        LambdaQueryWrapper<Course> courseWrapper = new LambdaQueryWrapper<Course>()
                .eq(Course::getStatus, 1)
                .ge(Course::getCourseDate, LocalDate.now())
                .orderByAsc(Course::getCourseDate).orderByAsc(Course::getStartTime);
        courseMapper.selectPage(coursePage, courseWrapper);
        fillCoachNames(coursePage.getRecords());
        data.put("upcomingCourses", coursePage.getRecords());

        // 用户已报名的课程
        List<UserCourse> myCourses = userCourseMapper.selectList(
                new LambdaQueryWrapper<UserCourse>().eq(UserCourse::getUserId, userId));
        List<Course> myCourseList = new ArrayList<>();
        if (!myCourses.isEmpty()) {
            myCourseList = courseMapper.selectBatchIds(
                    myCourses.stream().map(UserCourse::getCourseId).toList());
            fillCoachNames(myCourseList);
        }
        data.put("myCourses", myCourseList);

        // 推荐教练（在职的前5位）
        Page<Coach> coachPage = new Page<>(1, 5);
        LambdaQueryWrapper<Coach> coachWrapper = new LambdaQueryWrapper<Coach>()
                .eq(Coach::getStatus, 1)
                .orderByDesc(Coach::getCreatedAt);
        coachMapper.selectPage(coachPage, coachWrapper);
        data.put("coaches", coachPage.getRecords());

        return Result.ok(data);
    }

    private void fillCoachNames(List<Course> courses) {
        if (courses == null || courses.isEmpty()) return;
        List<Long> coachIds = courses.stream()
                .map(Course::getCoachId).filter(id -> id != null).distinct().toList();
        if (coachIds.isEmpty()) return;
        Map<Long, String> nameMap = coachMapper.selectBatchIds(coachIds).stream()
                .collect(Collectors.toMap(Coach::getId, Coach::getName));
        courses.forEach(c -> {
            if (c.getCoachId() != null) c.setCoachName(nameMap.get(c.getCoachId()));
        });
    }
}
