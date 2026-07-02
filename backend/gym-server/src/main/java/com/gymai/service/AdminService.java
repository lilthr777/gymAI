package com.gymai.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gymai.common.Result;
import com.gymai.dto.AdminDashboardDTO;
import com.gymai.dto.CoachScheduleDTO;
import com.gymai.dto.DashboardStatsDTO;
import com.gymai.entity.Coach;
import com.gymai.entity.Course;
import com.gymai.entity.User;

import java.util.List;

public interface AdminService {
    Result<AdminDashboardDTO> dashboard();
    Result<DashboardStatsDTO> stats();
    Result<Page<User>> users(int pageNum, int pageSize, String keyword, String role);
    Result<?> updateUser(Long id, User user);
    Result<?> deleteUser(Long id);
    Result<?> createCoach(Coach coach);
    Result<?> updateCoach(Long id, Coach coach);
    Result<?> deleteCoach(Long id);
    Result<?> createCourse(Course course);
    Result<?> updateCourse(Long id, Course course);
    Result<?> deleteCourse(Long id);
    Result<Page<Course>> adminCourses(int pageNum, int pageSize);
    Result<List<CoachScheduleDTO>> coachSchedule(Long coachUserId);
    Result<AdminDashboardDTO> coachDashboard(Long coachUserId);
}
