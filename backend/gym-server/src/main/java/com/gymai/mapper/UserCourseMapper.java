package com.gymai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gymai.entity.UserCourse;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserCourseMapper extends BaseMapper<UserCourse> {
    @Delete("DELETE FROM user_course WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);
}
