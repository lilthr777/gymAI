package com.gymai.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gymai.common.Result;
import com.gymai.entity.Course;
import com.gymai.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @GetMapping
    public Result<Page<Course>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long coachId) {
        return courseService.page(pageNum, pageSize, keyword, coachId);
    }

    @GetMapping("/{id}")
    public Result<Course> getById(@PathVariable Long id) {
        return courseService.getById(id);
    }

    @PostMapping
    public Result<?> save(@RequestBody Course course) {
        return courseService.save(course);
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody Course course) {
        course.setId(id);
        return courseService.update(course);
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        return courseService.delete(id);
    }
}
