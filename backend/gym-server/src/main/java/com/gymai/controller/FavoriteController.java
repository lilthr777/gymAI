package com.gymai.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gymai.common.Result;
import com.gymai.config.UserPrincipal;
import com.gymai.entity.*;
import com.gymai.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final UserFavoriteCoachMapper favoriteMapper;
    private final CoachMapper coachMapper;

    /** 收藏/取消收藏教练 */
    @PostMapping("/coach/{coachId}")
    @Transactional
    public Result<?> toggle(@AuthenticationPrincipal UserPrincipal principal,
                            @PathVariable Long coachId) {
        UserFavoriteCoach exist = favoriteMapper.selectOne(new LambdaQueryWrapper<UserFavoriteCoach>()
                .eq(UserFavoriteCoach::getUserId, principal.userId())
                .eq(UserFavoriteCoach::getCoachId, coachId));
        if (exist != null) {
            favoriteMapper.deleteById(exist);
            return Result.ok(Map.of("favorited", false));
        }
        UserFavoriteCoach f = new UserFavoriteCoach();
        f.setUserId(principal.userId());
        f.setCoachId(coachId);
        favoriteMapper.insert(f);
        return Result.ok(Map.of("favorited", true));
    }

    /** 我的收藏 */
    @GetMapping("/coaches")
    public Result<List<Coach>> myFavorites(@AuthenticationPrincipal UserPrincipal principal) {
        List<Long> coachIds = favoriteMapper.selectList(
                new LambdaQueryWrapper<UserFavoriteCoach>()
                        .eq(UserFavoriteCoach::getUserId, principal.userId()))
                .stream().map(UserFavoriteCoach::getCoachId).toList();
        if (coachIds.isEmpty()) return Result.ok(List.of());
        return Result.ok(coachMapper.selectBatchIds(coachIds));
    }

    /** 我收藏的教练 ID 集合 */
    @GetMapping("/coach-ids")
    public Result<List<Long>> myFavoriteIds(@AuthenticationPrincipal UserPrincipal principal) {
        return Result.ok(favoriteMapper.selectList(
                new LambdaQueryWrapper<UserFavoriteCoach>()
                        .eq(UserFavoriteCoach::getUserId, principal.userId()))
                .stream().map(UserFavoriteCoach::getCoachId).toList());
    }
}
