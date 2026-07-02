package com.gymai.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class AdminDashboardDTO {
    private Long totalUsers;
    private Long totalCourses;
    private Long totalCoaches;
    private Long todayCheckins;
    private BigDecimal monthlyRevenue;
}
