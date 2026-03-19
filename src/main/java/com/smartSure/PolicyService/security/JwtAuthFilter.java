package com.smartSure.PolicyService.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        //  Avoid re-authentication
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            //  1. PRIMARY → Gateway Headers (Most Important in Microservices)
            String userId = request.getHeader("X-User-Id");
            String role = request.getHeader("X-User-Role");

            if (userId != null && role != null) {
                log.info("Authenticated via Gateway headers: userId={}, role={}", userId, role);
                setAuthentication(userId, role);
                filterChain.doFilter(request, response);
                return;
            }

            //  2. FALLBACK → JWT Token (for Postman / Swagger testing)
            String authHeader = request.getHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {

                String token = authHeader.substring(7);

                if (jwtUtil.isTokenValid(token)) {

                    String extractedUserId = jwtUtil.extractUserId(token);
                    String extractedRole = jwtUtil.extractRole(token);

                    if (extractedUserId != null && extractedRole != null) {
                        log.info("Authenticated via JWT: userId={}, role={}", extractedUserId, extractedRole);
                        setAuthentication(extractedUserId, extractedRole);
                    }
                } else {
                    log.warn("Invalid JWT token");
                }
            }

        } catch (Exception ex) {
            log.error("Authentication error: {}", ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private void setAuthentication(String userId, String role) {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role))
                );

        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}