package com.gymai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gymai.entity.CourseReview;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CourseReviewMapper extends BaseMapper<CourseReview> {
    @Delete("DELETE FROM course_review WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);
}
