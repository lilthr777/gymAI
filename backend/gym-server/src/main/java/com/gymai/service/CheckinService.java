package com.gymai.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gymai.common.Result;
import com.gymai.entity.Checkin;

import java.util.Map;

public interface CheckinService {
    Result<Page<Checkin>> page(int pageNum, int pageSize, Long memberId, Long courseId);
    Result<?> checkin(Checkin checkin);
    Result<Map<String, Object>> stats();
}
