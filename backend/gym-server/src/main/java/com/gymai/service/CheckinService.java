package com.gymai.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gymai.common.Result;
import com.gymai.entity.Checkin;

public interface CheckinService {
    Result<?> checkin(Long userId, Long courseId);
    Result<Page<Checkin>> myCheckins(Long userId, int pageNum, int pageSize);
}
