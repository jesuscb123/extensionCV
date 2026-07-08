package com.jobmatch.ai.ai.groq.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** Cuerpo de la petición a {@code /chat/completions} (API compatible con OpenAI). */
public record ChatRequest(
        String model,
        List<ChatMessage> messages,
        Double temperature,
        @JsonProperty("response_format") ResponseFormat responseFormat) {
}
