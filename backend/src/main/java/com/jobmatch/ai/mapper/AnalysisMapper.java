package com.jobmatch.ai.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobmatch.ai.ai.AiAnalysis;
import com.jobmatch.ai.api.dto.AnalysisResultDto;
import com.jobmatch.ai.api.dto.AnalysisResultDto.AnalysisMetaDto;
import com.jobmatch.ai.api.dto.AnalysisResultDto.AtsCompatibilityDto;
import com.jobmatch.ai.exception.AiProviderException;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Component;

/** Convierte el JSON crudo de la IA en {@link AiAnalysis} y compone el DTO de salida. */
@Component
public class AnalysisMapper {

    private final ObjectMapper objectMapper;

    public AnalysisMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public AiAnalysis parse(String json) {
        try {
            return objectMapper.readValue(json, AiAnalysis.class);
        } catch (JsonProcessingException ex) {
            throw new AiProviderException("La IA devolvió un JSON no válido", ex);
        }
    }

    public AnalysisResultDto toResult(AiAnalysis ai, String model, String language) {
        AtsCompatibilityDto ats = ai.atsCompatibility() == null
                ? new AtsCompatibilityDto(0, List.of())
                : new AtsCompatibilityDto(clamp(ai.atsCompatibility().score()), nn(ai.atsCompatibility().issues()));

        return new AnalysisResultDto(
                clamp(ai.cvScore()),
                clamp(ai.coverLetterScore()),
                clamp(ai.globalMatch()),
                ats,
                nn(ai.strengths()),
                nn(ai.weaknesses()),
                nn(ai.missingKeywords()),
                nn(ai.missingSkills()),
                nn(ai.grammarIssues()),
                nn(ai.formattingIssues()),
                nn(ai.recommendations()),
                orEmpty(ai.improvedCv()),
                orEmpty(ai.improvedCoverLetter()),
                new AnalysisMetaDto(model, language, Instant.now().toString()));
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }

    private static List<String> nn(List<String> list) {
        return list == null ? List.of() : list;
    }

    private static String orEmpty(String value) {
        return value == null ? "" : value;
    }
}
