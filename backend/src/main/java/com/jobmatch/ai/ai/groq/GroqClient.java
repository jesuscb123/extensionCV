package com.jobmatch.ai.ai.groq;

import com.jobmatch.ai.ai.AiAnalysis;
import com.jobmatch.ai.ai.AiClient;
import com.jobmatch.ai.mapper.AnalysisMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Cliente real de IA para el análisis de CV/carta contra la API de Groq.
 * Activo cuando {@code ai.mode=groq}.
 */
@Component
@ConditionalOnProperty(name = "ai.mode", havingValue = "groq")
public class GroqClient implements AiClient {

    private final GroqChatClient chatClient;
    private final AnalysisMapper analysisMapper;

    public GroqClient(GroqChatClient chatClient, AnalysisMapper analysisMapper) {
        this.chatClient = chatClient;
        this.analysisMapper = analysisMapper;
    }

    @Override
    public AiAnalysis analyze(String systemPrompt, String userPrompt) {
        return analysisMapper.parse(chatClient.chat(systemPrompt, userPrompt));
    }
}
