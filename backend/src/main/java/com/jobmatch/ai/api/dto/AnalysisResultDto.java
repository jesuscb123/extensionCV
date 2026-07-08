package com.jobmatch.ai.api.dto;

import java.util.List;

/** Resultado completo del análisis que se devuelve a la extensión. */
public record AnalysisResultDto(
        int cvScore,
        int coverLetterScore,
        int globalMatch,
        AtsCompatibilityDto atsCompatibility,
        List<String> strengths,
        List<String> weaknesses,
        List<String> missingKeywords,
        List<String> missingSkills,
        List<String> grammarIssues,
        List<String> formattingIssues,
        List<String> recommendations,
        String improvedCv,
        String improvedCoverLetter,
        AnalysisMetaDto meta) {

    public record AtsCompatibilityDto(int score, List<String> issues) {
    }

    public record AnalysisMetaDto(String model, String language, String generatedAt) {
    }
}
