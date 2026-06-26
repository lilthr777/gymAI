package com.gymai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user_course")
public class UserCourse {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long courseId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime registerTime;
}
