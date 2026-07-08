package com.jobmatch.ai.api.dto;

/** Contexto opcional de la oferta para ayudar a redactar mejores respuestas. Todo es opcional. */
public record JobContextRequest(String title, String company, String description) {
}
