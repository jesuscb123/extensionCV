package com.jobmatch.ai.ai.groq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobmatch.ai.ai.AiAnswer;
import com.jobmatch.ai.ai.AiAnswerBatch;
import com.jobmatch.ai.ai.QuestionAnswerAiClient;
import com.jobmatch.ai.api.dto.AnswerQuestionRequest;
import com.jobmatch.ai.exception.AiProviderException;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Cliente real de IA para el modo "responder preguntas" contra la API de Groq.
 * Activo cuando {@code ai.mode=groq}.
 */
@Component
@ConditionalOnProperty(name = "ai.mode", havingValue = "groq")
public class GroqQuestionAnswerClient implements QuestionAnswerAiClient {

    private final GroqChatClient chatClient;
    private final ObjectMapper objectMapper;

    public GroqQuestionAnswerClient(GroqChatClient chatClient, ObjectMapper objectMapper) {
        this.chatClient = chatClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<AiAnswer> answer(String systemPrompt, String userPrompt, List<AnswerQuestionRequest> questions) {
        String rawJson = chatClient.chat(systemPrompt, userPrompt);
        try {
            AiAnswerBatch batch = objectMapper.readValue(rawJson, AiAnswerBatch.class);
            return batch.answers() == null ? List.of() : batch.answers();
        } catch (JsonProcessingException ex) {
            throw new AiProviderException("La IA devolvió un JSON no válido", ex);
        }
    }
}
