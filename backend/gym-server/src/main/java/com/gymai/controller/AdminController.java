package com.gymai.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gymai.common.Result;
import com.gymai.config.UserPrincipal;
import com.gymai.dto.AdminDashboardDTO;
import com.gymai.dto.DashboardStatsDTO;
import com.gymai.entity.Coach;
import com.gymai.entity.Course;
import com.gymai.entity.User;
import com.gymai.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    /** 管理后台仪表盘 */
    @GetMapping("/dashboard")
    public Result<AdminDashboardDTO> dashboard() {
        return adminService.dashboard();
    }

    /** 图表统计数据 */
    @GetMapping("/dashboard/stats")
    public Result<DashboardStatsDTO> stats() {
        return adminService.stats();
    }

    /** 用户列表（支持按ID/用户名搜索 + 角色筛选） */
    @GetMapping("/users")
    public Result<Page<User>> users(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role) {
        return adminService.users(pageNum, pageSize, keyword, role);
    }

    /** 编辑用户（角色、状态） */
    @PutMapping("/users/{id}")
    public Result<?> updateUser(@PathVariable Long id, @RequestBody User user) {
        return adminService.updateUser(id, user);
    }

    /** 删除用户（硬删除，同时清理关联数据） */
    @DeleteMapping("/users/{id}")
    public Result<?> deleteUser(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        if (principal.userId().equals(id)) {
            return Result.error(400, "不能删除自己的账号");
        }
        return adminService.deleteUser(id);
    }

    /** 新增教练 */
    @PostMapping("/coaches")
    public Result<?> createCoach(@RequestBody Coach coach) {
        return adminService.createCoach(coach);
    }

    /** 编辑教练 */
    @PutMapping("/coaches/{id}")
    public Result<?> updateCoach(@PathVariable Long id, @RequestBody Coach coach) {
        return adminService.updateCoach(id, coach);
    }

    /** 删除教练（软删除） */
    @DeleteMapping("/coaches/{id}")
    public Result<?> deleteCoach(@PathVariable Long id) {
        return adminService.deleteCoach(id);
    }

    /** 管理端课程列表 */
    @GetMapping("/courses")
    public Result<Page<Course>> adminCourses(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return adminService.adminCourses(pageNum, pageSize);
    }

    /** 新增课程 */
    @PostMapping("/courses")
    public Result<?> createCourse(@RequestBody Course course) {
        return adminService.createCourse(course);
    }

    /** 编辑课程 */
    @PutMapping("/courses/{id}")
    public Result<?> updateCourse(@PathVariable Long id, @RequestBody Course course) {
        return adminService.updateCourse(id, course);
    }

    /** 删除课程（取消） */
    @DeleteMapping("/courses/{id}")
    public Result<?> deleteCourse(@PathVariable Long id) {
        return adminService.deleteCourse(id);
    }
}
