package com.gymai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gymai.common.Result;
import com.gymai.entity.Member;
import com.gymai.mapper.MemberMapper;
import com.gymai.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberMapper memberMapper;

    @Override
    public Result<Page<Member>> page(int pageNum, int pageSize, String keyword) {
        Page<Member> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Member::getName, keyword).or().like(Member::getPhone, keyword);
        }
        wrapper.orderByDesc(Member::getCreatedAt);
        memberMapper.selectPage(page, wrapper);
        return Result.ok(page);
    }

    @Override
    public Result<Member> getById(Long id) {
        return Result.ok(memberMapper.selectById(id));
    }

    @Override
    public Result<?> save(Member member) {
        memberMapper.insert(member);
        return Result.ok();
    }

    @Override
    public Result<?> update(Member member) {
        memberMapper.updateById(member);
        return Result.ok();
    }

    @Override
    public Result<?> delete(Long id) {
        memberMapper.deleteById(id);
        return Result.ok();
    }
}
