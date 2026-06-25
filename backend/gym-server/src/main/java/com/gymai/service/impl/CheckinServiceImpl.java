package com.gymai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gymai.common.Result;
import com.gymai.entity.Checkin;
import com.gymai.mapper.CheckinMapper;
import com.gymai.mapper.CourseMapper;
import com.gymai.mapper.MemberMapper;
import com.gymai.service.CheckinService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CheckinServiceImpl implements CheckinService {

    private final CheckinMapper checkinMapper;
    private final MemberMapper memberMapper;
    private final CourseMapper courseMapper;

    @Override
    public Result<Page<Checkin>> page(int pageNum, int pageSize, Long memberId, Long courseId) {
        Page<Checkin> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Checkin> wrapper = new LambdaQueryWrapper<>();
        if (memberId != null) wrapper.eq(Checkin::getMemberId, memberId);
        if (courseId != null) wrapper.eq(Checkin::getCourseId, courseId);
        wrapper.orderByDesc(Checkin::getCheckinTime);
        checkinMapper.selectPage(page, wrapper);
        return Result.ok(page);
    }

    @Override
    public Result<?> checkin(Checkin checkin) {
        checkinMapper.insert(checkin);
        return Result.ok();
    }

    @Override
    public Result<Map<String, Object>> stats() {
        Map<String, Object> data = new HashMap<>();
        data.put("totalMembers", memberMapper.selectCount(null));
        data.put("totalCoaches", 0L);
        data.put("totalCourses", courseMapper.selectCount(null));
        data.put("todayCheckins", checkinMapper.selectCount(
                new LambdaQueryWrapper<Checkin>()
                        .ge(Checkin::getCheckinTime, LocalDate.now().atStartOfDay())));
        return Result.ok(data);
    }
}
