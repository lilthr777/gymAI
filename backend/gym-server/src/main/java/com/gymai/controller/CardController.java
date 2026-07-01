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

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/card")
@RequiredArgsConstructor
public class CardController {

    private final UserMapper userMapper;
    private final CardOrderMapper cardOrderMapper;

    /** 获取当前用户的会员卡信息 + 购买记录 */
    @GetMapping
    public Result<Map<String, Object>> info(@AuthenticationPrincipal UserPrincipal principal) {
        User user = userMapper.selectById(principal.userId());
        Map<String, Object> data = new HashMap<>();
        data.put("cardType", user.getCardType());
        data.put("cardStartDate", user.getCardStartDate());
        data.put("cardEndDate", user.getCardEndDate());

        // 购买记录
        List<CardOrder> orders = cardOrderMapper.selectList(
                new LambdaQueryWrapper<CardOrder>()
                        .eq(CardOrder::getUserId, principal.userId())
                        .orderByDesc(CardOrder::getCreatedAt));
        data.put("orders", orders);
        return Result.ok(data);
    }

    /** 续费 / 开卡 */
    @PostMapping("/renew")
    @Transactional
    public Result<?> renew(@AuthenticationPrincipal UserPrincipal principal,
                           @RequestBody Map<String, String> body) {
        String cardType = body.get("cardType");
        if (cardType == null || !List.of("MONTH", "QUARTER", "YEAR", "LIFETIME").contains(cardType)) {
            return Result.error(400, "无效的卡类型");
        }

        User user = userMapper.selectById(principal.userId());
        LocalDate startDate = LocalDate.now();
        // 如果原卡未到期，续费从到期日开始算
        if (user.getCardEndDate() != null && user.getCardEndDate().isAfter(startDate)) {
            startDate = user.getCardEndDate();
        }

        LocalDate endDate = switch (cardType) {
            case "MONTH" -> startDate.plusMonths(1);
            case "QUARTER" -> startDate.plusMonths(3);
            case "YEAR" -> startDate.plusYears(1);
            default -> null;
        };

        int amount = switch (cardType) {
            case "MONTH" -> 299;
            case "QUARTER" -> 799;
            case "YEAR" -> 2599;
            default -> 0;
        };

        // 记录订单
        CardOrder order = new CardOrder();
        order.setUserId(principal.userId());
        order.setCardType(cardType);
        order.setAmount(amount);
        order.setStatus(1);
        cardOrderMapper.insert(order);

        // 更新用户会员卡
        user.setCardType(cardType);
        user.setCardStartDate(startDate);
        user.setCardEndDate(endDate);
        userMapper.updateById(user);

        return Result.ok(Map.of("startDate", startDate.toString(), "endDate",
                endDate != null ? endDate.toString() : ""));
    }
}
