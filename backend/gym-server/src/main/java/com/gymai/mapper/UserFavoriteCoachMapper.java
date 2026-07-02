package com.gymai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gymai.entity.UserFavoriteCoach;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserFavoriteCoachMapper extends BaseMapper<UserFavoriteCoach> {
    @Delete("DELETE FROM user_favorite_coach WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);

    @Delete("DELETE FROM user_favorite_coach WHERE coach_id = #{coachId}")
    int deleteByCoachId(@Param("coachId") Long coachId);
}
