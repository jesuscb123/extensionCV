package com.jobmatch.ai.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

/** Una pregunta detectada en un formulario de candidatura (cualquier portal de empleo). */
public record AnswerQuestionRequest(
        @NotBlank String id,
        @NotBlank @Size(max = 500) String label,
        String fieldType,
        Integer maxLength,
        List<String> options) {
}
