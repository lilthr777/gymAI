package com.gymai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gymai.entity.CardOrder;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CardOrderMapper extends BaseMapper<CardOrder> {
    @Delete("DELETE FROM card_order WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);
}
