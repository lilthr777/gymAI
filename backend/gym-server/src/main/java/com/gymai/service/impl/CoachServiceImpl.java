package com.gymai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gymai.common.Result;
import com.gymai.entity.Coach;
import com.gymai.mapper.CoachMapper;
import com.gymai.service.CoachService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class CoachServiceImpl implements CoachService {

    private final CoachMapper coachMapper;

    @Override
    public Result<Page<Coach>> page(int pageNum, int pageSize, String keyword) {
        Page<Coach> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Coach> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Coach::getStatus, 1);
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Coach::getName, keyword).or().like(Coach::getSpecialty, keyword);
        }
        wrapper.orderByDesc(Coach::getCreatedAt);
        coachMapper.selectPage(page, wrapper);
        return Result.ok(page);
    }

    @Override
    public Result<Coach> getById(Long id) {
        return Result.ok(coachMapper.selectById(id));
    }
}
