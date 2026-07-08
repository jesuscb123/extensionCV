package com.jobmatch.ai.ai.groq;

import com.jobmatch.ai.ai.groq.model.ChatMessage;
import com.jobmatch.ai.ai.groq.model.ChatRequest;
import com.jobmatch.ai.ai.groq.model.ChatResponse;
import com.jobmatch.ai.ai.groq.model.ResponseFormat;
import com.jobmatch.ai.config.AiProperties;
import com.jobmatch.ai.exception.AiProviderException;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Llamada HTTP de bajo nivel a {@code /chat/completions} de Groq (API compatible con OpenAI),
 * compartida por los distintos clientes de IA por feature ({@link GroqClient},
 * {@link GroqQuestionAnswerClient}). Cada uno interpreta el JSON de respuesta a su manera.
 */
@Component
public class GroqChatClient {

    private final AiProperties properties;
    private final RestClient restClient;

    public GroqChatClient(AiProperties properties, RestClient.Builder builder) {
        this.properties = properties;
        this.restClient = builder
                .baseUrl(properties.baseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.apiKey())
                .build();
    }

    /** Devuelve el contenido crudo (texto JSON) del mensaje generado por el modelo. */
    public String chat(String systemPrompt, String userPrompt) {
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

            return extractContent(response);
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
