package com.gymai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gymai.common.Result;
import com.gymai.entity.Course;
import com.gymai.mapper.CourseMapper;
import com.gymai.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseMapper courseMapper;

    @Override
    public Result<Page<Course>> page(int pageNum, int pageSize, String keyword) {
        Page<Course> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Course::getName, keyword);
        }
        wrapper.orderByDesc(Course::getCourseDate).orderByAsc(Course::getStartTime);
        courseMapper.selectPage(page, wrapper);
        return Result.ok(page);
    }

    @Override
    public Result<Course> getById(Long id) {
        return Result.ok(courseMapper.selectById(id));
    }

    @Override
    public Result<?> save(Course course) {
        courseMapper.insert(course);
        return Result.ok();
    }

    @Override
    public Result<?> update(Course course) {
        courseMapper.updateById(course);
        return Result.ok();
    }

    @Override
    public Result<?> delete(Long id) {
        courseMapper.deleteById(id);
        return Result.ok();
    }
}
