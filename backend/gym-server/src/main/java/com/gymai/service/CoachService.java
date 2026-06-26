package com.gymai.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gymai.common.Result;
import com.gymai.entity.Coach;

public interface CoachService {
    Result<Page<Coach>> page(int pageNum, int pageSize, String keyword);
    Result<Coach> getById(Long id);
}
