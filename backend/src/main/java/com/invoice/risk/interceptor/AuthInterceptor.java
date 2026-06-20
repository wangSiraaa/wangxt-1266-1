package com.invoice.risk.interceptor;

import com.invoice.risk.context.UserContext;
import com.invoice.risk.entity.SysUser;
import com.invoice.risk.enums.RoleEnum;
import com.invoice.risk.repository.SysUserRepository;
import com.invoice.risk.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final SysUserRepository sysUserRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String uri = request.getRequestURI();
        if (uri.contains("/auth/login") ||
            uri.contains("/v3/api-docs") ||
            uri.contains("/swagger-ui") ||
            uri.contains("/swagger-ui.html")) {
            return true;
        }

        String token = resolveToken(request);
        if (token == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        try {
            Claims claims = jwtUtil.parseToken(token);
            Long userId = ((Number) claims.get("userId")).longValue();
            SysUser user = sysUserRepository.findById(userId).orElse(null);

            if (user == null || !user.getEnabled()) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }

            UserContext.setUser(user);
            return true;
        } catch (Exception e) {
            log.warn("Auth failed: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
