package com.gymai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gymai.common.Result;
import com.gymai.entity.Coach;
import com.gymai.entity.Course;
import com.gymai.entity.UserCourse;
import com.gymai.mapper.CoachMapper;
import com.gymai.mapper.CourseMapper;
import com.gymai.mapper.UserCourseMapper;
import com.gymai.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseMapper courseMapper;
    private final UserCourseMapper userCourseMapper;
    private final CoachMapper coachMapper;

    /** 给课程列表批量填充教练名 */
    private void fillCoachNames(List<Course> courses) {
        if (courses == null || courses.isEmpty()) return;
        List<Long> coachIds = courses.stream()
                .map(Course::getCoachId)
                .filter(id -> id != null)
                .distinct()
                .toList();
        if (coachIds.isEmpty()) return;
        Map<Long, String> nameMap = coachMapper.selectBatchIds(coachIds).stream()
                .collect(Collectors.toMap(Coach::getId, Coach::getName));
        courses.forEach(c -> {
            if (c.getCoachId() != null) c.setCoachName(nameMap.get(c.getCoachId()));
        });
    }

    /** 标记课程列表中当前用户已报名的课程 */
    private void markUserRegistered(List<Course> courses, Long userId) {
        if (courses == null || courses.isEmpty() || userId == null) return;
        List<Long> courseIds = courses.stream().map(Course::getId).filter(id -> id != null).toList();
        if (courseIds.isEmpty()) return;
        List<Long> registeredIds = userCourseMapper.selectList(
                new LambdaQueryWrapper<UserCourse>()
                        .eq(UserCourse::getUserId, userId)
                        .in(UserCourse::getCourseId, courseIds))
                .stream().map(UserCourse::getCourseId).toList();
        courses.forEach(c -> c.setRegistered(registeredIds.contains(c.getId())));
    }

    @Override
    public Result<Page<Course>> page(int pageNum, int pageSize, String keyword, Long coachId, Long userId) {
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
        fillCoachNames(page.getRecords());
        markUserRegistered(page.getRecords(), userId);
        return Result.ok(page);
    }

    @Override
    public Result<Course> getById(Long id) {
        return getById(id, null);
    }

    @Override
    public Result<Course> getById(Long id, Long userId) {
        Course course = courseMapper.selectById(id);
        if (course != null) {
            fillCoachNames(List.of(course));
            markUserRegistered(List.of(course), userId);
        }
        return Result.ok(course);
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
        List<Course> records = ucPage.getRecords().stream()
                .map(uc -> courseMapper.selectById(uc.getCourseId()))
                .toList();
        fillCoachNames(records);
        result.setRecords(records);
        return Result.ok(result);
    }
}
