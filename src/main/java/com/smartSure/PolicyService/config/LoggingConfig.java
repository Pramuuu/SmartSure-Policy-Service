package com.smartSure.PolicyService.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Injects per-request context into the MDC (Mapped Diagnostic Context).
 *
 * Every log line printed during a request will automatically carry:
 *   requestId  — unique ID for this HTTP request
 *   userId     — from X-User-Id header (set by gateway)
 *   userRole   — from X-User-Role header
 *   method     — GET / POST / PUT etc.
 *   path       — /api/policies/purchase etc.
 *
 * These fields appear in Loki and can be queried in Grafana.
 */
@Component
@Order(1)
public class LoggingConfig extends OncePerRequestFilter {

    private static final String REQUEST_ID = "requestId";
    private static final String USER_ID    = "userId";
    private static final String USER_ROLE  = "userRole";
    private static final String HTTP_METHOD = "method";
    private static final String HTTP_PATH   = "path";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String requestId = UUID.randomUUID().toString().substring(0, 8);
            MDC.put(REQUEST_ID,  requestId);
            MDC.put(USER_ID,     orNone(request.getHeader("X-User-Id")));
            MDC.put(USER_ROLE,   orNone(request.getHeader("X-User-Role")));
            MDC.put(HTTP_METHOD, request.getMethod());
            MDC.put(HTTP_PATH,   request.getRequestURI());

            // Put requestId in response header so API clients can correlate
            response.setHeader("X-Request-Id", requestId);

            filterChain.doFilter(request, response);
        } finally {
            // ALWAYS clear MDC — thread pool reuse will bleed context otherwise
            MDC.clear();
        }
    }

    private String orNone(String value) {
        return value != null ? value : "none";
    }
}
