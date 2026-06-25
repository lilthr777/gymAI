package com.gymai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("member_course")
public class MemberCourse {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long memberId;
    private Long courseId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime registerTime;
}
