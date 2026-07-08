package com.jobmatch.ai.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Respuesta generada por la IA para una pregunta concreta, identificada por su {@code id}. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AiAnswer(String id, String answer) {
}
