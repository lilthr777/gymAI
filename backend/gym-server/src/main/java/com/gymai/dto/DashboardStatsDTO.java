package com.gymai.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class DashboardStatsDTO {
    private List<MonthCount> enrollmentTrend;
    private List<MonthAmount> revenueByMonth;

    @Data
    public static class MonthCount {
        private String month;
        private Long count;
    }

    @Data
    public static class MonthAmount {
        private String month;
        private BigDecimal amount;
    }
}
