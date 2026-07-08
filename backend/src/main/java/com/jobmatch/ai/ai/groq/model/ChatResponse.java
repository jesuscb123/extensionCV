package com.jobmatch.ai.ai.groq.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/** Respuesta de {@code /chat/completions}; solo nos interesa el contenido del mensaje. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ChatResponse(List<Choice> choices) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Choice(ChatMessage message) {
    }
}
