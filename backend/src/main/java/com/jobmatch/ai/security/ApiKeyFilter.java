package com.jobmatch.ai.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobmatch.ai.api.dto.ErrorCode;
import com.jobmatch.ai.config.AppSecurityProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Exige la cabecera {@code X-Api-Key} en los endpoints de análisis. Si no hay API key
 * configurada, la autenticación se desactiva (modo desarrollo local).
 */
public class ApiKeyFilter extends OncePerRequestFilter {

    private static final String HEADER = "X-Api-Key";

    private final AppSecurityProperties properties;
    private final ObjectMapper objectMapper;

    public ApiKeyFilter(AppSecurityProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String configured = properties.apiKey();
        boolean authRequired = configured != null && !configured.isBlank() && requiresAuth(request);

        if (authRequired && !configured.equals(request.getHeader(HEADER))) {
            SecurityResponses.writeError(response, objectMapper,
                    HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED, "API key inválida o ausente");
            return;
        }
        chain.doFilter(request, response);
    }

    private boolean requiresAuth(HttpServletRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return false;
        }
        return ProtectedPaths.matches(request.getRequestURI());
    }
}
