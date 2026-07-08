package com.jobmatch.ai.ai.groq.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ChatMessage(String role, String content) {
}
