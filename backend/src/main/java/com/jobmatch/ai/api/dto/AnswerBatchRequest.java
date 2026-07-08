package com.jobmatch.ai.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

/** Lote de preguntas detectadas en un formulario de candidatura, a responder con IA a partir del CV. */
public record AnswerBatchRequest(
        @NotEmpty @Valid List<AnswerQuestionRequest> questions,
        JobContextRequest jobOffer,
        @Size(max = 20) String language) {
}
