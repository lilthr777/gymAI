package com.gymai.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = extractToken(request);
        if (StringUtils.hasText(token) && jwtUtil.validateToken(token)) {
            Long userId = jwtUtil.getUserIdFromToken(token);
            String username = jwtUtil.getUsernameFromToken(token);
            String role = jwtUtil.getRoleFromToken(token);
            authenticate(userId, username, role);
        } else if ("127.0.0.1".equals(request.getRemoteAddr()) || "0:0:0:0:0:0:0:1".equals(request.getRemoteAddr())) {
            // 内部服务调用：通过 X-User-Id 头认证
            String userIdHeader = request.getHeader("X-User-Id");
            if (StringUtils.hasText(userIdHeader)) {
                try {
                    Long userId = Long.parseLong(userIdHeader);
                    authenticate(userId, "internal", "MEMBER");
                } catch (NumberFormatException ignored) {}
            }
        }
        filterChain.doFilter(request, response);
    }

    private void authenticate(Long userId, String username, String role) {
        // 将 JWT 中的 role 映射为 Spring Security 权限
        if (role == null || role.isEmpty()) {
            role = "MEMBER";
        }
        String authority = switch (role.toUpperCase()) {
            case "ADMIN" -> "ROLE_ADMIN";
            case "COACH" -> "ROLE_COACH";
            default -> "ROLE_USER";
        };
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(authority));
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(new UserPrincipal(userId, username, role), null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
