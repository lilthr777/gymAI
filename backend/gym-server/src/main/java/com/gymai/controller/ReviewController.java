package com.gymai.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gymai.common.Result;
import com.gymai.config.UserPrincipal;
import com.gymai.entity.*;
import com.gymai.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final CourseReviewMapper reviewMapper;
    private final UserMapper userMapper;

    /** 课程评价列表 */
    @GetMapping("/course/{courseId}")
    public Result<Page<Map<String, Object>>> list(@PathVariable Long courseId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        Page<CourseReview> page = new Page<>(pageNum, pageSize);
        reviewMapper.selectPage(page, new LambdaQueryWrapper<CourseReview>()
                .eq(CourseReview::getCourseId, courseId)
                .orderByDesc(CourseReview::getCreatedAt));
        // 填用户昵称
        List<Map<String, Object>> records = new ArrayList<>();
        for (CourseReview r : page.getRecords()) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", r.getId());
            item.put("rating", r.getRating());
            item.put("comment", r.getComment());
            item.put("createdAt", r.getCreatedAt());
            User u = userMapper.selectById(r.getUserId());
            item.put("nickname", u != null ? u.getNickname() : "匿名");
            records.add(item);
        }
        Page<Map<String, Object>> result = new Page<>(pageNum, pageSize, page.getTotal());
        result.setRecords(records);
        return Result.ok(result);
    }

    /** 提交评价 */
    @PostMapping
    @Transactional
    public Result<?> submit(@AuthenticationPrincipal UserPrincipal principal,
                            @RequestBody Map<String, Object> body) {
        Long courseId = Long.valueOf(body.get("courseId").toString());
        int rating = Integer.parseInt(body.get("rating").toString());
        if (rating < 1 || rating > 5) return Result.error(400, "评分范围 1-5");
        String comment = (String) body.getOrDefault("comment", "");

        // 检查是否已评价
        Long count = reviewMapper.selectCount(new LambdaQueryWrapper<CourseReview>()
                .eq(CourseReview::getUserId, principal.userId())
                .eq(CourseReview::getCourseId, courseId));
        if (count > 0) return Result.error(400, "已评价过该课程");

        CourseReview review = new CourseReview();
        review.setUserId(principal.userId());
        review.setCourseId(courseId);
        review.setRating(rating);
        review.setComment(comment);
        reviewMapper.insert(review);
        return Result.ok();
    }
}
