package com.gymai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("coach")
public class Coach {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String phone;
    private Integer gender;
    private String specialty;
    private String description;
    private String avatar;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
