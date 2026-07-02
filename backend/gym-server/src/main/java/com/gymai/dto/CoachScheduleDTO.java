package com.gymai.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class CoachScheduleDTO {
    private Long courseId;
    private String courseName;
    private LocalDate courseDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String location;
    private BigDecimal price;
    private Integer maxCapacity;
    private Integer currentCount;
    private String memberName;
    private String memberPhone;
}
