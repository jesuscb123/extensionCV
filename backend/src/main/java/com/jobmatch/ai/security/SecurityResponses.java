package com.jobmatch.ai.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobmatch.ai.api.dto.ApiError;
import com.jobmatch.ai.api.dto.ApiResponse;
import com.jobmatch.ai.api.dto.ErrorCode;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

/** Utilidad para escribir el envelope de error directamente desde los filtros de seguridad. */
final class SecurityResponses {

    private SecurityResponses() {
    }

    static void writeError(HttpServletResponse response, ObjectMapper objectMapper,
                           HttpStatus status, ErrorCode code, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        ApiResponse<Object> body = ApiResponse.fail(ApiError.of(code, message));
        objectMapper.writeValue(response.getWriter(), body);
    }
}
