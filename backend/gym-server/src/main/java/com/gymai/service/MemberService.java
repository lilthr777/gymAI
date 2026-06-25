package com.gymai.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gymai.common.Result;
import com.gymai.entity.Member;

public interface MemberService {
    Result<Page<Member>> page(int pageNum, int pageSize, String keyword);
    Result<Member> getById(Long id);
    Result<?> save(Member member);
    Result<?> update(Member member);
    Result<?> delete(Long id);
}
