package com.gymai.controller;

import com.gymai.tools.GymDataService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.*;

@RestController
@RequestMapping("/ai")
@CrossOrigin(origins = "*")
public class AiChatController {

    private final ChatClient chatClient;
    private final GymDataService gymDataService;

    public AiChatController(ChatClient.Builder builder, GymDataService gymDataService) {
        this.gymDataService = gymDataService;
        this.chatClient = builder
                .defaultFunctions("searchCourses", "getCoaches")
                .defaultSystem("""
                        你是一个健身房 AI 助手，帮助用户浏览课程、查看教练信息。
                        当用户询问课程相关问题，使用 searchCourses 函数查询。
                        当用户询问教练相关问题，使用 getCoaches 函数查询。
                        用户个人信息会作为上下文附在消息开头，请直接据此回答个人相关问题，无需调用函数。
                        用中文回答，语气友好简练。""")
                .build();
    }

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(@RequestBody Map<String, String> body) {
        String message = body.getOrDefault("message", "");
        Long userId = parseUserId(body.getOrDefault("userId", ""));

        // 查用户数据拼到消息里，避免 Reactive 线程切换丢上下文
        String userContext = buildUserContext(userId);
        String fullMessage = userContext + "\n用户问题: " + message;

        return chatClient.prompt()
                .user(fullMessage)
                .stream()
                .content();
    }

    private Long parseUserId(String s) {
        try { return Long.parseLong(s); } catch (NumberFormatException e) { return null; }
    }

    private String buildUserContext(Long userId) {
        if (userId == null) return "[用户未登录]";
        List<Map<String, Object>> courses = gymDataService.getMyCourses(userId);
        List<Map<String, Object>> checkins = gymDataService.getMyCheckins(userId);
        return "[用户ID: " + userId
                + ", 已报名课程: " + courses
                + ", 签到记录(共" + checkins.size() + "次): " + checkins + "]";
    }
}
