package com.gymai.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gymai.common.Result;
import com.gymai.entity.Course;

public interface CourseService {
    Result<Page<Course>> page(int pageNum, int pageSize, String keyword, Long coachId);
    Result<Course> getById(Long id);
    Result<?> save(Course course);
    Result<?> update(Course course);
    Result<?> delete(Long id);
}
