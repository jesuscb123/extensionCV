package com.jobmatch.ai.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.jobmatch.ai.api.dto.AnalysisResultDto.AtsCompatibilityDto;
import java.util.List;

/** Análisis tal y como lo devuelve la IA (sin metadatos, que añade el backend). */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AiAnalysis(
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
        String improvedCoverLetter) {
}
