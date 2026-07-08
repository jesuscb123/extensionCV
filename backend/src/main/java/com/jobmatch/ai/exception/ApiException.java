package com.jobmatch.ai.exception;

import com.jobmatch.ai.api.dto.ErrorCode;

/** Excepción de negocio que traslada un {@link ErrorCode} al envelope de la API. */
public class ApiException extends RuntimeException {

    private final transient ErrorCode code;

    public ApiException(ErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    public ErrorCode code() {
        return code;
    }
}
