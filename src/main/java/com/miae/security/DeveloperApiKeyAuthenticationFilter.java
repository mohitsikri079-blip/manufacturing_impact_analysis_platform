package com.miae.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * DeveloperApiKeyAuthenticationFilter is a filter that checks for a developer API key 
 * in the request headers and authenticates the request if the key is valid.
 */
public class DeveloperApiKeyAuthenticationFilter extends OncePerRequestFilter {
    private final SecurityProperties.ApiKey apiKey;
    public DeveloperApiKeyAuthenticationFilter(SecurityProperties.ApiKey apiKey) { this.apiKey = apiKey; }
    @Override protected boolean shouldNotFilter(HttpServletRequest request) { return !request.getRequestURI().startsWith("/api/"); }
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String supplied = request.getHeader(apiKey.getHeaderName());
        String expected = apiKey.getValue();
        if (expected != null && supplied != null && MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), supplied.getBytes(StandardCharsets.UTF_8))) {
            SecurityContextHolder.getContext().setAuthentication(UsernamePasswordAuthenticationToken.authenticated(
                    "developer-api-key", null, List.of(new SimpleGrantedAuthority("ROLE_API_CLIENT"))));
        }
        chain.doFilter(request, response);
    }
}
