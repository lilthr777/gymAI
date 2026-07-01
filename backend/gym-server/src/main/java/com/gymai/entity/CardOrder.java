package com.gymai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("card_order")
public class CardOrder {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String cardType;
    private Integer amount;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
