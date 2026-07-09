package com.jobmatch.ai.api.dto;

import org.springframework.http.HttpStatus;

/** Códigos de error estables del envelope de la API, con su estado HTTP asociado. */
public enum ErrorCode {
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED),
    PAYLOAD_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE),
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE),
    PDF_UNPROCESSABLE(HttpStatus.UNPROCESSABLE_ENTITY),
    RATE_LIMITED(HttpStatus.TOO_MANY_REQUESTS),
    JOB_NOT_FOUND(HttpStatus.NOT_FOUND),
    CV_NOT_FOUND(HttpStatus.NOT_FOUND),
    AI_PROVIDER_ERROR(HttpStatus.BAD_GATEWAY),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR);

    private final HttpStatus status;

    ErrorCode(HttpStatus status) {
        this.status = status;
    }

    public HttpStatus status() {
        return status;
    }
}
