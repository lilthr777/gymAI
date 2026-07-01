package com.gymai.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gymai.common.Result;
import com.gymai.entity.Course;

public interface CourseService {
    Result<Page<Course>> page(int pageNum, int pageSize, String keyword, Long coachId, Long userId);
    Result<Course> getById(Long id);
    Result<Course> getById(Long id, Long userId);
    Result<?> register(Long userId, Long courseId);
    Result<?> cancel(Long userId, Long courseId);
    Result<Page<Course>> myCourses(Long userId, int pageNum, int pageSize);
}
