package com.gymai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gymai.entity.Member;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MemberMapper extends BaseMapper<Member> {
}
