package com.jobmatch.ai.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(ErrorCode code, String message, List<ErrorDetail> details) {

    public static ApiError of(ErrorCode code, String message) {
        return new ApiError(code, message, null);
    }
}
