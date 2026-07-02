package com.gymai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gymai.common.Result;
import com.gymai.dto.AdminDashboardDTO;
import com.gymai.dto.CoachScheduleDTO;
import com.gymai.dto.DashboardStatsDTO;
import com.gymai.entity.*;
import com.gymai.mapper.*;
import com.gymai.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserMapper userMapper;
    private final CoachMapper coachMapper;
    private final CourseMapper courseMapper;
    private final CheckinMapper checkinMapper;
    private final UserCourseMapper userCourseMapper;
    private final CardOrderMapper cardOrderMapper;
    private final CourseReviewMapper courseReviewMapper;
    private final UserFavoriteCoachMapper userFavoriteCoachMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Result<AdminDashboardDTO> dashboard() {
        AdminDashboardDTO dto = new AdminDashboardDTO();
        dto.setTotalUsers(userMapper.selectCount(null));
        dto.setTotalCourses(courseMapper.selectCount(
                new LambdaQueryWrapper<Course>().ne(Course::getStatus, 0)));
        dto.setTotalCoaches(coachMapper.selectCount(
                new LambdaQueryWrapper<Coach>().eq(Coach::getStatus, 1)));
        dto.setTodayCheckins(checkinMapper.selectCount(
                new LambdaQueryWrapper<Checkin>()
                        .ge(Checkin::getCreatedAt, LocalDate.now().atStartOfDay())));
        // 本月收入
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        List<CardOrder> orders = cardOrderMapper.selectList(
                new LambdaQueryWrapper<CardOrder>()
                        .ge(CardOrder::getCreatedAt, monthStart)
                        .eq(CardOrder::getStatus, 1));
        double revenue = orders.stream()
                .mapToInt(o -> o.getAmount() != null ? o.getAmount() : 0)
                .sum();
        dto.setMonthlyRevenue(BigDecimal.valueOf(revenue));
        return Result.ok(dto);
    }

    @Override
    public Result<DashboardStatsDTO> stats() {
        DashboardStatsDTO dto = new DashboardStatsDTO();
        // 最近6个月的报名趋势
        List<DashboardStatsDTO.MonthCount> enrollmentTrend = new ArrayList<>();
        List<DashboardStatsDTO.MonthAmount> revenueByMonth = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        LocalDate now = LocalDate.now();

        for (int i = 5; i >= 0; i--) {
            LocalDate month = now.minusMonths(i);
            String label = month.format(formatter);
            LocalDateTime start = month.withDayOfMonth(1).atStartOfDay();
            LocalDateTime end = month.plusMonths(1).withDayOfMonth(1).atStartOfDay();

            Long count = userCourseMapper.selectCount(
                    new LambdaQueryWrapper<UserCourse>().between(UserCourse::getRegisterTime, start, end));
            DashboardStatsDTO.MonthCount mc = new DashboardStatsDTO.MonthCount();
            mc.setMonth(label);
            mc.setCount(count);
            enrollmentTrend.add(mc);

            List<CardOrder> orders = cardOrderMapper.selectList(
                    new LambdaQueryWrapper<CardOrder>()
                            .between(CardOrder::getCreatedAt, start, end)
                            .eq(CardOrder::getStatus, 1));
            double amount = orders.stream()
                    .mapToInt(o -> o.getAmount() != null ? o.getAmount() : 0)
                    .sum();
            DashboardStatsDTO.MonthAmount ma = new DashboardStatsDTO.MonthAmount();
            ma.setMonth(label);
            ma.setAmount(BigDecimal.valueOf(amount));
            revenueByMonth.add(ma);
        }
        dto.setEnrollmentTrend(enrollmentTrend);
        dto.setRevenueByMonth(revenueByMonth);
        return Result.ok(dto);
    }

    @Override
    public Result<Page<User>> users(int pageNum, int pageSize, String keyword, String role) {
        Page<User> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            // 尝试按 ID 精确匹配，或按用户名模糊搜索
            try {
                Long id = Long.parseLong(keyword);
                wrapper.and(w -> w.eq(User::getId, id).or().like(User::getUsername, keyword));
            } catch (NumberFormatException e) {
                wrapper.like(User::getUsername, keyword);
            }
        }
        if (StringUtils.hasText(role)) {
            wrapper.eq(User::getRole, role);
        }
        wrapper.orderByDesc(User::getCreatedAt);
        userMapper.selectPage(page, wrapper);
        // 移除敏感字段
        page.getRecords().forEach(u -> u.setPassword(null));
        return Result.ok(page);
    }

    @Override
    public Result<?> updateUser(Long id, User updateData) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        if (updateData.getRole() != null) {
            user.setRole(updateData.getRole());
        }
        if (updateData.getNickname() != null) {
            user.setNickname(updateData.getNickname());
        }
        if (updateData.getPhone() != null) {
            user.setPhone(updateData.getPhone());
        }
        userMapper.updateById(user);
        return Result.ok();
    }

    @Override
    @Transactional
    public Result<?> deleteUser(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        // 删除用户关联数据（使用显式 SQL 确保可靠删除）
        checkinMapper.deleteByUserId(id);
        userCourseMapper.deleteByUserId(id);
        cardOrderMapper.deleteByUserId(id);
        courseReviewMapper.deleteByUserId(id);
        userFavoriteCoachMapper.deleteByUserId(id);

        // 如果该用户是教练，还需要清理教练相关数据
        if ("COACH".equals(user.getRole())) {
            // 找到对应的教练记录（通过手机号关联）
            Coach coach = coachMapper.selectOne(
                    new LambdaQueryWrapper<Coach>().eq(Coach::getPhone, user.getPhone()));
            if (coach != null) {
                // 取消该教练的所有课程
                courseMapper.update(null,
                        new LambdaUpdateWrapper<Course>()
                                .eq(Course::getCoachId, coach.getId())
                                .set(Course::getStatus, 0));
                // 删除其他用户对该教练的收藏
                userFavoriteCoachMapper.deleteByCoachId(coach.getId());
                // 删除教练记录
                coachMapper.deleteById(coach.getId());
            }
        }

        // 硬删除用户
        userMapper.deleteById(id);
        return Result.ok();
    }

    @Override
    @Transactional
    public Result<?> createCoach(Coach coach) {
        coach.setStatus(1);
        coachMapper.insert(coach);
        // 自动为教练创建登录账号
        User existUser = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getPhone, coach.getPhone()));
        if (existUser == null) {
            User coachUser = new User();
            coachUser.setUsername("coach_" + coach.getPhone());
            coachUser.setPhone(coach.getPhone());
            coachUser.setPassword(passwordEncoder.encode("123456"));
            coachUser.setNickname(coach.getName());
            coachUser.setRole("COACH");
            userMapper.insert(coachUser);
        }
        return Result.ok();
    }

    @Override
    public Result<?> updateCoach(Long id, Coach updateData) {
        Coach coach = coachMapper.selectById(id);
        if (coach == null) {
            return Result.error(404, "教练不存在");
        }
        if (updateData.getName() != null) coach.setName(updateData.getName());
        if (updateData.getPhone() != null) coach.setPhone(updateData.getPhone());
        if (updateData.getGender() != null) coach.setGender(updateData.getGender());
        if (updateData.getSpecialty() != null) coach.setSpecialty(updateData.getSpecialty());
        if (updateData.getDescription() != null) coach.setDescription(updateData.getDescription());
        if (updateData.getAvatar() != null) coach.setAvatar(updateData.getAvatar());
        coachMapper.updateById(coach);
        return Result.ok();
    }

    @Override
    public Result<?> deleteCoach(Long id) {
        Coach coach = coachMapper.selectById(id);
        if (coach == null) {
            return Result.error(404, "教练不存在");
        }
        coach.setStatus(0);
        coachMapper.updateById(coach);
        return Result.ok();
    }

    @Override
    public Result<?> createCourse(Course course) {
        course.setCurrentCount(0);
        course.setStatus(1);
        courseMapper.insert(course);
        return Result.ok();
    }

    @Override
    public Result<?> updateCourse(Long id, Course updateData) {
        Course course = courseMapper.selectById(id);
        if (course == null) {
            return Result.error(404, "课程不存在");
        }
        if (updateData.getName() != null) course.setName(updateData.getName());
        if (updateData.getCoachId() != null) course.setCoachId(updateData.getCoachId());
        if (updateData.getCourseDate() != null) course.setCourseDate(updateData.getCourseDate());
        if (updateData.getStartTime() != null) course.setStartTime(updateData.getStartTime());
        if (updateData.getEndTime() != null) course.setEndTime(updateData.getEndTime());
        if (updateData.getMaxCapacity() != null) course.setMaxCapacity(updateData.getMaxCapacity());
        if (updateData.getPrice() != null) course.setPrice(updateData.getPrice());
        if (updateData.getDescription() != null) course.setDescription(updateData.getDescription());
        if (updateData.getLocation() != null) course.setLocation(updateData.getLocation());
        if (updateData.getStatus() != null) course.setStatus(updateData.getStatus());
        courseMapper.updateById(course);
        return Result.ok();
    }

    @Override
    public Result<?> deleteCourse(Long id) {
        Course course = courseMapper.selectById(id);
        if (course == null) {
            return Result.error(404, "课程不存在");
        }
        course.setStatus(0);
        courseMapper.updateById(course);
        return Result.ok();
    }

    @Override
    public Result<Page<Course>> adminCourses(int pageNum, int pageSize) {
        Page<Course> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Course::getCourseDate).orderByDesc(Course::getStartTime);
        courseMapper.selectPage(page, wrapper);
        // 填充教练名
        List<Course> records = page.getRecords();
        if (!records.isEmpty()) {
            List<Long> coachIds = records.stream()
                    .map(Course::getCoachId).filter(Objects::nonNull).distinct().toList();
            if (!coachIds.isEmpty()) {
                Map<Long, String> nameMap = coachMapper.selectBatchIds(coachIds).stream()
                        .collect(Collectors.toMap(Coach::getId, Coach::getName));
                records.forEach(c -> {
                    if (c.getCoachId() != null) c.setCoachName(nameMap.get(c.getCoachId()));
                });
            }
        }
        return Result.ok(page);
    }

    @Override
    public Result<List<CoachScheduleDTO>> coachSchedule(Long coachUserId) {
        // 通过教练用户ID找到对应的教练记录（通过手机号关联）
        User coachUser = userMapper.selectById(coachUserId);
        if (coachUser == null || !"COACH".equals(coachUser.getRole())) {
            return Result.error(403, "非教练用户");
        }
        Coach coach = coachMapper.selectOne(
                new LambdaQueryWrapper<Coach>().eq(Coach::getPhone, coachUser.getPhone()));
        if (coach == null) {
            return Result.error(404, "未找到教练信息");
        }
        // 查该教练的所有课程
        List<Course> courses = courseMapper.selectList(
                new LambdaQueryWrapper<Course>()
                        .eq(Course::getCoachId, coach.getId())
                        .ne(Course::getStatus, 0)
                        .orderByAsc(Course::getCourseDate).orderByAsc(Course::getStartTime));
        List<CoachScheduleDTO> result = new ArrayList<>();
        for (Course course : courses) {
            // 查已报名的会员
            List<UserCourse> enrollments = userCourseMapper.selectList(
                    new LambdaQueryWrapper<UserCourse>().eq(UserCourse::getCourseId, course.getId()));
            if (enrollments.isEmpty()) {
                CoachScheduleDTO dto = new CoachScheduleDTO();
                fillCourseToScheduleDTO(course, dto);
                result.add(dto);
            } else {
                List<Long> userIds = enrollments.stream().map(UserCourse::getUserId).toList();
                Map<Long, User> userMap = userMapper.selectBatchIds(userIds).stream()
                        .collect(Collectors.toMap(User::getId, u -> u));
                for (UserCourse enrollment : enrollments) {
                    CoachScheduleDTO dto = new CoachScheduleDTO();
                    fillCourseToScheduleDTO(course, dto);
                    User member = userMap.get(enrollment.getUserId());
                    if (member != null) {
                        dto.setMemberName(member.getNickname() != null ? member.getNickname() : member.getUsername());
                        dto.setMemberPhone(member.getPhone());
                    }
                    result.add(dto);
                }
            }
        }
        return Result.ok(result);
    }

    private void fillCourseToScheduleDTO(Course course, CoachScheduleDTO dto) {
        dto.setCourseId(course.getId());
        dto.setCourseName(course.getName());
        dto.setCourseDate(course.getCourseDate());
        dto.setStartTime(course.getStartTime());
        dto.setEndTime(course.getEndTime());
        dto.setLocation(course.getLocation());
        dto.setPrice(course.getPrice());
        dto.setMaxCapacity(course.getMaxCapacity());
        dto.setCurrentCount(course.getCurrentCount());
    }

    @Override
    public Result<AdminDashboardDTO> coachDashboard(Long coachUserId) {
        User coachUser = userMapper.selectById(coachUserId);
        if (coachUser == null || !"COACH".equals(coachUser.getRole())) {
            return Result.error(403, "非教练用户");
        }
        Coach coach = coachMapper.selectOne(
                new LambdaQueryWrapper<Coach>().eq(Coach::getPhone, coachUser.getPhone()));
        if (coach == null) {
            return Result.error(404, "未找到教练信息");
        }
        AdminDashboardDTO dto = new AdminDashboardDTO();
        // 该教练的总课程数
        dto.setTotalCourses(courseMapper.selectCount(
                new LambdaQueryWrapper<Course>()
                        .eq(Course::getCoachId, coach.getId())
                        .ne(Course::getStatus, 0)));
        // 该教练的总学员数（通过课程报名去重）
        List<Course> coachCourses = courseMapper.selectList(
                new LambdaQueryWrapper<Course>().eq(Course::getCoachId, coach.getId()));
        if (!coachCourses.isEmpty()) {
            List<Long> courseIds = coachCourses.stream().map(Course::getId).toList();
            List<UserCourse> enrollments = userCourseMapper.selectList(
                    new LambdaQueryWrapper<UserCourse>().in(UserCourse::getCourseId, courseIds));
            long uniqueStudents = enrollments.stream().map(UserCourse::getUserId).distinct().count();
            dto.setTotalUsers(uniqueStudents);
        } else {
            dto.setTotalUsers(0L);
        }
        // 今日签到（该教练的课程）
        if (!coachCourses.isEmpty()) {
            List<Long> courseIds = coachCourses.stream().map(Course::getId).toList();
            dto.setTodayCheckins(checkinMapper.selectCount(
                    new LambdaQueryWrapper<Checkin>()
                            .in(Checkin::getCourseId, courseIds)
                            .ge(Checkin::getCreatedAt, LocalDate.now().atStartOfDay())));
        } else {
            dto.setTodayCheckins(0L);
        }
        dto.setTotalCoaches(1L);
        return Result.ok(dto);
    }
}
