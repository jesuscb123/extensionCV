package com.jobmatch.ai.ai;

import com.jobmatch.ai.api.dto.AnswerQuestionRequest;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Implementación de desarrollo: genera una respuesta de ejemplo determinista por cada
 * pregunta, sin llamar a ninguna API externa. Activa cuando {@code ai.mode=stub} (por defecto).
 */
@Component
@ConditionalOnProperty(name = "ai.mode", havingValue = "stub", matchIfMissing = true)
public class StubQuestionAnswerClient implements QuestionAnswerAiClient {

    @Override
    public List<AiAnswer> answer(String systemPrompt, String userPrompt, List<AnswerQuestionRequest> questions) {
        return questions.stream()
                .map(q -> new AiAnswer(q.id(), "Respuesta de ejemplo (stub) para: " + q.label()))
                .toList();
    }
}
