package com.gymai.tools;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Configuration
public class ToolConfig {

    @Bean
    @Description("搜索课程，keyword为空时返回所有可报名课程")
    public Function<SearchCoursesRequest, String> searchCourses(GymDataService service) {
        return req -> {
            List<Map<String, Object>> courses = service.searchCourses(req.keyword);
            return courses.isEmpty() ? "暂无课程" : courses.toString();
        };
    }

    @Bean
    @Description("查询所有在职教练及其擅长领域")
    public Function<GetCoachesRequest, String> getCoaches(GymDataService service) {
        return req -> {
            List<Map<String, Object>> coaches = service.getCoaches();
            return coaches.isEmpty() ? "暂无教练" : coaches.toString();
        };
    }

    public record SearchCoursesRequest(String keyword) {}
    public record GetCoachesRequest() {}
}
