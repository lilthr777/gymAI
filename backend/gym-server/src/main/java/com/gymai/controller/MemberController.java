package com.gymai.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gymai.common.Result;
import com.gymai.entity.Member;
import com.gymai.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping
    public Result<Page<Member>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        return memberService.page(pageNum, pageSize, keyword);
    }

    @GetMapping("/{id}")
    public Result<Member> getById(@PathVariable Long id) {
        return memberService.getById(id);
    }

    @PostMapping
    public Result<?> save(@RequestBody Member member) {
        return memberService.save(member);
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody Member member) {
        member.setId(id);
        return memberService.update(member);
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        return memberService.delete(id);
    }
}
