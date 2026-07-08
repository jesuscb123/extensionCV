package com.jobmatch.ai.ai.groq;

import com.jobmatch.ai.ai.AiAnalysis;
import com.jobmatch.ai.ai.AiClient;
import com.jobmatch.ai.ai.groq.model.ChatMessage;
import com.jobmatch.ai.ai.groq.model.ChatRequest;
import com.jobmatch.ai.ai.groq.model.ChatResponse;
import com.jobmatch.ai.ai.groq.model.ResponseFormat;
import com.jobmatch.ai.config.AiProperties;
import com.jobmatch.ai.exception.AiProviderException;
import com.jobmatch.ai.mapper.AnalysisMapper;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Cliente real de IA contra la API de Groq (compatible con OpenAI).
 * Activo cuando {@code ai.mode=groq}.
 */
@Component
@ConditionalOnProperty(name = "ai.mode", havingValue = "groq")
public class GroqClient implements AiClient {

    private final AiProperties properties;
    private final AnalysisMapper analysisMapper;
    private final RestClient restClient;

    public GroqClient(AiProperties properties, AnalysisMapper analysisMapper, RestClient.Builder builder) {
        this.properties = properties;
        this.analysisMapper = analysisMapper;
        this.restClient = builder
                .baseUrl(properties.baseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.apiKey())
                .build();
    }

    @Override
    public AiAnalysis analyze(String systemPrompt, String userPrompt) {
        ChatRequest request = new ChatRequest(
                properties.model(),
                List.of(new ChatMessage("system", systemPrompt), new ChatMessage("user", userPrompt)),
                properties.temperature(),
                ResponseFormat.jsonObject());

        try {
            ChatResponse response = restClient.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(ChatResponse.class);

            return analysisMapper.parse(extractContent(response));
        } catch (RestClientException ex) {
            throw new AiProviderException("No se pudo obtener respuesta de Groq", ex);
        }
    }

    private String extractContent(ChatResponse response) {
        if (response == null || response.choices() == null || response.choices().isEmpty()) {
            throw new AiProviderException("Respuesta vacía del proveedor de IA");
        }
        ChatMessage message = response.choices().get(0).message();
        if (message == null || message.content() == null || message.content().isBlank()) {
            throw new AiProviderException("El proveedor de IA no devolvió contenido");
        }
        return message.content();
    }
}
