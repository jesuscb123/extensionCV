package com.jobmatch.ai.api.dto;

/** Envelope consistente de todas las respuestas de la API: {@code {success, data, error}}. */
public record ApiResponse<T>(boolean success, T data, ApiError error) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static <T> ApiResponse<T> fail(ApiError error) {
        return new ApiResponse<>(false, null, error);
    }
}
