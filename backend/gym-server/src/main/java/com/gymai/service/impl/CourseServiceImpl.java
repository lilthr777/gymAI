package com.gymai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gymai.common.Result;
import com.gymai.entity.Course;
import com.gymai.entity.UserCourse;
import com.gymai.mapper.CourseMapper;
import com.gymai.mapper.UserCourseMapper;
import com.gymai.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseMapper courseMapper;
    private final UserCourseMapper userCourseMapper;

    @Override
    public Result<Page<Course>> page(int pageNum, int pageSize, String keyword, Long coachId) {
        Page<Course> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Course::getStatus, 1);
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Course::getName, keyword);
        }
        if (coachId != null) {
            wrapper.eq(Course::getCoachId, coachId);
        }
        wrapper.orderByAsc(Course::getCourseDate).orderByAsc(Course::getStartTime);
        courseMapper.selectPage(page, wrapper);
        return Result.ok(page);
    }

    @Override
    public Result<Course> getById(Long id) {
        return Result.ok(courseMapper.selectById(id));
    }

    @Override
    @Transactional
    public Result<?> register(Long userId, Long courseId) {
        Course course = courseMapper.selectById(courseId);
        if (course == null || course.getStatus() == 0) {
            return Result.error(400, "课程不存在或已取消");
        }
        if (course.getCurrentCount() >= course.getMaxCapacity()) {
            return Result.error(400, "课程已满员");
        }
        UserCourse exist = userCourseMapper.selectOne(
                new LambdaQueryWrapper<UserCourse>()
                        .eq(UserCourse::getUserId, userId)
                        .eq(UserCourse::getCourseId, courseId));
        if (exist != null) {
            return Result.error(400, "已报名该课程");
        }
        UserCourse uc = new UserCourse();
        uc.setUserId(userId);
        uc.setCourseId(courseId);
        userCourseMapper.insert(uc);
        course.setCurrentCount(course.getCurrentCount() + 1);
        if (course.getCurrentCount() >= course.getMaxCapacity()) {
            course.setStatus(2);
        }
        courseMapper.updateById(course);
        return Result.ok();
    }

    @Override
    @Transactional
    public Result<?> cancel(Long userId, Long courseId) {
        UserCourse uc = userCourseMapper.selectOne(
                new LambdaQueryWrapper<UserCourse>()
                        .eq(UserCourse::getUserId, userId)
                        .eq(UserCourse::getCourseId, courseId));
        if (uc == null) {
            return Result.error(400, "未报名该课程");
        }
        userCourseMapper.deleteById(uc);
        Course course = courseMapper.selectById(courseId);
        if (course != null) {
            course.setCurrentCount(Math.max(0, course.getCurrentCount() - 1));
            if (course.getStatus() == 2) {
                course.setStatus(1);
            }
            courseMapper.updateById(course);
        }
        return Result.ok();
    }

    @Override
    public Result<Page<Course>> myCourses(Long userId, int pageNum, int pageSize) {
        Page<UserCourse> ucPage = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<UserCourse> wrapper = new LambdaQueryWrapper<UserCourse>()
                .eq(UserCourse::getUserId, userId)
                .orderByDesc(UserCourse::getRegisterTime);
        userCourseMapper.selectPage(ucPage, wrapper);

        Page<Course> result = new Page<>(pageNum, pageSize, ucPage.getTotal());
        result.setRecords(ucPage.getRecords().stream()
                .map(uc -> courseMapper.selectById(uc.getCourseId()))
                .toList());
        return Result.ok(result);
    }
}
