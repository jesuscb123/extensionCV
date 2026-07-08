package com.jobmatch.ai.ai;

import com.jobmatch.ai.api.dto.AnalysisResultDto.AtsCompatibilityDto;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Implementación de desarrollo: devuelve un análisis de ejemplo determinista sin llamar
 * a ninguna API externa. Activa cuando {@code ai.mode=stub} (valor por defecto).
 */
@Component
@ConditionalOnProperty(name = "ai.mode", havingValue = "stub", matchIfMissing = true)
public class StubAiClient implements AiClient {

    @Override
    public AiAnalysis analyze(String systemPrompt, String userPrompt) {
        AtsCompatibilityDto ats = new AtsCompatibilityDto(
                72,
                List.of("Añade una sección de aptitudes con las palabras clave de la oferta"));

        return new AiAnalysis(
                74,
                68,
                71,
                ats,
                List.of("Experiencia relevante para el sector", "Buen dominio técnico"),
                List.of("Faltan logros cuantificados", "Resumen poco enfocado a la oferta"),
                List.of("Kubernetes", "CI/CD"),
                List.of("Docker", "AWS"),
                List.of("Revisar la concordancia en el segundo párrafo"),
                List.of("Usa un formato de una sola columna para mejorar la lectura ATS"),
                List.of("Adapta el titular del CV a la oferta", "Incluye métricas de impacto"),
                "CV mejorado (versión de ejemplo generada por el stub de desarrollo).",
                "Carta mejorada (versión de ejemplo generada por el stub de desarrollo).");
    }
}
