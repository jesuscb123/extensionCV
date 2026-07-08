package com.jobmatch.ai.ai.groq;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobmatch.ai.ai.AiAnalysis;
import com.jobmatch.ai.config.AiProperties;
import com.jobmatch.ai.mapper.AnalysisMapper;
import com.jobmatch.ai.prompt.PromptBuilder;
import com.jobmatch.ai.prompt.PromptContext;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.web.client.RestClient;
import org.yaml.snakeyaml.Yaml;

/**
 * Prueba en vivo contra la API real de Groq. Desactivada por defecto; se activa con
 * {@code -Dgroq.live=true}. Lee la API key de {@code config/application.yml} (no versionado),
 * por lo que no expone el secreto en la línea de comandos ni en el repositorio.
 */
@EnabledIfSystemProperty(named = "groq.live", matches = "true")
class GroqLiveTest {

    @Test
    void realGroqReturnsStructuredAnalysis() {
        String apiKey = readApiKey();
        Assumptions.assumeTrue(apiKey != null && apiKey.startsWith("gsk_"),
                "No hay API key de Groq en config/application.yml");

        AiProperties properties = new AiProperties(
                "groq", "https://api.groq.com/openai/v1", apiKey, "llama-3.3-70b-versatile", 0.2);
        GroqClient client = new GroqClient(properties, new AnalysisMapper(new ObjectMapper()), RestClient.builder());

        PromptBuilder promptBuilder = new PromptBuilder();
        PromptContext context = new PromptContext(
                "Backend Engineer", "Acme Corp", "Madrid",
                "Buscamos ingeniero backend con Java, Spring Boot, REST, PostgreSQL y Docker.",
                "CV: Ana Garcia. Ingeniera backend con 5 anos en Java, Spring Boot, REST y PostgreSQL.",
                "Carta: candidata motivada con experiencia solida en Java y Spring Boot.",
                "es");

        AiAnalysis analysis = client.analyze(promptBuilder.system(), promptBuilder.user(context));

        assertThat(analysis).isNotNull();
        assertThat(analysis.cvScore()).isBetween(0, 100);
        assertThat(analysis.coverLetterScore()).isBetween(0, 100);
        assertThat(analysis.globalMatch()).isBetween(0, 100);
        assertThat(analysis.improvedCv()).isNotBlank();

        System.out.printf(
                "[GROQ LIVE] cvScore=%d coverLetterScore=%d globalMatch=%d atsScore=%d strengths=%d missingKeywords=%s%n",
                analysis.cvScore(), analysis.coverLetterScore(), analysis.globalMatch(),
                analysis.atsCompatibility() == null ? -1 : analysis.atsCompatibility().score(),
                analysis.strengths() == null ? 0 : analysis.strengths().size(),
                analysis.missingKeywords());
    }

    @SuppressWarnings("unchecked")
    private static String readApiKey() {
        Path config = Path.of("config", "application.yml");
        if (!Files.exists(config)) {
            return null;
        }
        try (InputStream in = Files.newInputStream(config)) {
            Map<String, Object> root = new Yaml().load(in);
            Map<String, Object> ai = (Map<String, Object>) root.get("ai");
            return ai == null ? null : (String) ai.get("api-key");
        } catch (Exception ex) {
            return null;
        }
    }
}
