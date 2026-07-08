package com.jobmatch.ai.prompt;

/** Datos que consume el {@link PromptBuilder} para construir el prompt de análisis. */
public record PromptContext(
        String jobTitle,
        String company,
        String location,
        String jobDescription,
        String cvText,
        String coverLetterText,
        String language) {
}
