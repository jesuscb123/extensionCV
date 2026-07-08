package com.jobmatch.ai.api.dto;

/** Pregunta ya respondida, lista para mostrarse en la extensión. */
public record AnsweredQuestionDto(String id, String label, String answer) {
}
