package com.jobmatch.ai.exception;

import com.jobmatch.ai.api.dto.ApiError;
import com.jobmatch.ai.api.dto.ApiResponse;
import com.jobmatch.ai.api.dto.ErrorCode;
import com.jobmatch.ai.api.dto.ErrorDetail;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

/** Traduce cualquier excepción al envelope {@code {success:false, error}} con su código estable. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Object>> handleApiException(ApiException ex) {
        return build(ex.code(), ex.getMessage(), null);
    }

    @ExceptionHandler(JobNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleJobNotFound(JobNotFoundException ex) {
        return build(ErrorCode.JOB_NOT_FOUND, ex.getMessage(), null);
    }

    @ExceptionHandler(CvNotStoredException.class)
    public ResponseEntity<ApiResponse<Object>> handleCvNotStored(CvNotStoredException ex) {
        return build(ErrorCode.CV_NOT_FOUND, ex.getMessage(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidation(MethodArgumentNotValidException ex) {
        List<ErrorDetail> details = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new ErrorDetail(error.getField(), error.getDefaultMessage()))
                .toList();
        return build(ErrorCode.VALIDATION_ERROR, "Datos de la oferta inválidos", details);
    }

    @ExceptionHandler({MissingServletRequestPartException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<ApiResponse<Object>> handleMissingPart(Exception ex) {
        return build(ErrorCode.VALIDATION_ERROR, ex.getMessage(), null);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Object>> handleTooLarge(MaxUploadSizeExceededException ex) {
        return build(ErrorCode.PAYLOAD_TOO_LARGE, "El archivo supera el tamaño máximo permitido", null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleUnexpected(Exception ex) {
        return build(ErrorCode.INTERNAL_ERROR, "Error interno del servidor", null);
    }

    private ResponseEntity<ApiResponse<Object>> build(ErrorCode code, String message, List<ErrorDetail> details) {
        HttpStatus status = code.status();
        ApiError error = new ApiError(code, message, details);
        return ResponseEntity.status(status).body(ApiResponse.fail(error));
    }
}
