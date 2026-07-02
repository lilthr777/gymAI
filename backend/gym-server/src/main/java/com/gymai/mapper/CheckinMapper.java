package com.gymai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gymai.entity.Checkin;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CheckinMapper extends BaseMapper<Checkin> {
    @Delete("DELETE FROM checkin WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);
}
