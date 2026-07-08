package com.jobmatch.ai.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobmatch.ai.api.dto.ErrorCode;
import com.jobmatch.ai.config.AppSecurityProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Rate limiting por IP con ventana fija en memoria. Ligero y sin dependencias; puede
 * sustituirse por Bucket4j si se necesita un algoritmo más sofisticado o distribuido.
 */
public class RateLimitFilter extends OncePerRequestFilter {

    private final AppSecurityProperties properties;
    private final ObjectMapper objectMapper;
    private final ConcurrentMap<String, Window> windows = new ConcurrentHashMap<>();

    public RateLimitFilter(AppSecurityProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();
        boolean protectedPath = uri.startsWith("/api/v1/analyses") || uri.startsWith("/api/v1/answers");
        if ("OPTIONS".equalsIgnoreCase(request.getMethod()) || !protectedPath) {
            chain.doFilter(request, response);
            return;
        }

        if (!isAllowed(clientIp(request))) {
            response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(properties.rateLimit().windowSeconds()));
            SecurityResponses.writeError(response, objectMapper,
                    HttpStatus.TOO_MANY_REQUESTS, ErrorCode.RATE_LIMITED, "Demasiadas peticiones, inténtalo más tarde");
            return;
        }
        chain.doFilter(request, response);
    }

    private boolean isAllowed(String ip) {
        long now = Instant.now().getEpochSecond();
        int limit = properties.rateLimit().requests();
        long windowSeconds = properties.rateLimit().windowSeconds();

        Window window = windows.compute(ip, (key, existing) -> {
            if (existing == null || now - existing.start >= windowSeconds) {
                return new Window(now);
            }
            existing.count++;
            return existing;
        });
        return window.count <= limit;
    }

    private static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static final class Window {
        private final long start;
        private int count;

        private Window(long start) {
            this.start = start;
            this.count = 1;
        }
    }
}
