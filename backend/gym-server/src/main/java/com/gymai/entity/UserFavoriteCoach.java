package com.gymai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user_favorite_coach")
public class UserFavoriteCoach {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long coachId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
