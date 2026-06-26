package com.gymai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gymai.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
