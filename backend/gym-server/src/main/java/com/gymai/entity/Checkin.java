package com.gymai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("checkin")
public class Checkin {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long memberId;
    private Long courseId;
    private LocalDateTime checkinTime;
    private Integer status;
    private String remark;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
