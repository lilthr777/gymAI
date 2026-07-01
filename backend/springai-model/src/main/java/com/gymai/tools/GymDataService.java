package com.gymai.tools;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.util.*;

@Service
public class GymDataService {

    private final RestClient restClient;

    public GymDataService() {
        this.restClient = RestClient.builder()
                .baseUrl("http://localhost:8080/api")
                .build();
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getMyCourses(Long userId) {
        try {
            var resp = restClient.get()
                    .uri("/courses/my?pageNum=1&pageSize=50")
                    .header("X-User-Id", String.valueOf(userId))
                    .retrieve()
                    .body(Map.class);
            if (resp != null && resp.get("data") != null) {
                return (List<Map<String, Object>>) ((Map<String, Object>) resp.get("data")).get("records");
            }
        } catch (Exception ignored) {}
        return List.of();
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> searchCourses(String keyword) {
        try {
            var resp = restClient.get()
                    .uri("/courses?pageNum=1&pageSize=50&keyword=" + (keyword != null ? keyword : ""))
                    .retrieve()
                    .body(Map.class);
            if (resp != null && resp.get("data") != null) {
                return (List<Map<String, Object>>) ((Map<String, Object>) resp.get("data")).get("records");
            }
        } catch (Exception ignored) {}
        return List.of();
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getCoaches() {
        try {
            var resp = restClient.get()
                    .uri("/coaches?pageNum=1&pageSize=50")
                    .retrieve()
                    .body(Map.class);
            if (resp != null && resp.get("data") != null) {
                return (List<Map<String, Object>>) ((Map<String, Object>) resp.get("data")).get("records");
            }
        } catch (Exception ignored) {}
        return List.of();
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getMyCheckins(Long userId) {
        try {
            var resp = restClient.get()
                    .uri("/checkins/my?pageNum=1&pageSize=50")
                    .header("X-User-Id", String.valueOf(userId))
                    .retrieve()
                    .body(Map.class);
            if (resp != null && resp.get("data") != null) {
                return (List<Map<String, Object>>) ((Map<String, Object>) resp.get("data")).get("records");
            }
        } catch (Exception ignored) {}
        return List.of();
    }
}
