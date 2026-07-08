package com.jobmatch.ai.ai;

import com.jobmatch.ai.api.dto.AnswerQuestionRequest;
import java.util.List;

/**
 * Abstracción del proveedor de IA para el modo "responder preguntas" (DIP).
 * Implementaciones: Groq (real) y stub (dev). El parámetro {@code questions} solo lo usa
 * el stub para generar respuestas de ejemplo sin depender del texto libre de los prompts.
 */
public interface QuestionAnswerAiClient {

    List<AiAnswer> answer(String systemPrompt, String userPrompt, List<AnswerQuestionRequest> questions);
}
