package com.miae.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor for validating API keys in incoming requests.
 */
@Component
public class ApiKeyInterceptor implements HandlerInterceptor {

    private static final String API_KEY_HEADER = "X-API-Key";

    private final String apiKey;
    private final boolean enabled;

    public ApiKeyInterceptor(
            @Value("${miae.security.api-key}") String apiKey,
            @Value("${miae.security.enabled}") boolean enabled) {
        this.apiKey = apiKey;
        this.enabled = enabled;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!enabled || apiKey.equals(request.getHeader(API_KEY_HEADER))) {
            return true;
        }
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        response.getWriter().write("{\"message\":\"Missing or invalid API key\"}");
        return false;
    }
}
