package com.gymai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gymai.common.Result;
import com.gymai.entity.Checkin;
import com.gymai.entity.Course;
import com.gymai.entity.UserCourse;
import com.gymai.mapper.CheckinMapper;
import com.gymai.mapper.CourseMapper;
import com.gymai.mapper.UserCourseMapper;
import com.gymai.service.CheckinService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CheckinServiceImpl implements CheckinService {

    private final CheckinMapper checkinMapper;
    private final UserCourseMapper userCourseMapper;
    private final CourseMapper courseMapper;

    @Override
    public Result<?> checkin(Long userId, Long courseId) {
        UserCourse uc = userCourseMapper.selectOne(
                new LambdaQueryWrapper<UserCourse>()
                        .eq(UserCourse::getUserId, userId)
                        .eq(UserCourse::getCourseId, courseId));
        if (uc == null) {
            return Result.error(400, "您未报名该课程");
        }
        Checkin exist = checkinMapper.selectOne(
                new LambdaQueryWrapper<Checkin>()
                        .eq(Checkin::getUserId, userId)
                        .eq(Checkin::getCourseId, courseId));
        if (exist != null) {
            return Result.error(400, "已签到，请勿重复签到");
        }
        Checkin checkin = new Checkin();
        checkin.setUserId(userId);
        checkin.setCourseId(courseId);
        checkin.setCheckinTime(LocalDateTime.now());
        checkin.setStatus(1);
        checkinMapper.insert(checkin);
        return Result.ok();
    }

    @Override
    public Result<Page<Checkin>> myCheckins(Long userId, int pageNum, int pageSize) {
        Page<Checkin> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Checkin> wrapper = new LambdaQueryWrapper<Checkin>()
                .eq(Checkin::getUserId, userId)
                .orderByDesc(Checkin::getCheckinTime);
        checkinMapper.selectPage(page, wrapper);
        // 填充课程名
        List<Long> courseIds = page.getRecords().stream().map(Checkin::getCourseId).distinct().toList();
        if (!courseIds.isEmpty()) {
            Map<Long, String> nameMap = courseMapper.selectBatchIds(courseIds).stream()
                    .collect(Collectors.toMap(Course::getId, Course::getName));
            page.getRecords().forEach(c -> c.setCourseName(nameMap.get(c.getCourseId())));
        }
        return Result.ok(page);
    }
}
