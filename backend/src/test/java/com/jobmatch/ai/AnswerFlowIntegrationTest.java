package com.jobmatch.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobmatch.ai.api.dto.AnswerBatchRequest;
import com.jobmatch.ai.api.dto.AnswerQuestionRequest;
import com.jobmatch.ai.support.PdfTestFactory;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(properties = "ai.mode=stub")
@AutoConfigureMockMvc
class AnswerFlowIntegrationTest {

    private static final String API_KEY = "test-key";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void fullFlowProducesAnswersForEachQuestion() throws Exception {
        MvcResult created = mockMvc.perform(multipart("/api/v1/answers")
                        .file(jsonPart())
                        .file(pdfPart("cv", "CV: Ana Garcia, ingeniera backend con 5 anos en Java y Spring Boot"))
                        .header("X-Api-Key", API_KEY))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.jobId").isNotEmpty())
                .andReturn();

        String jobId = readJson(created).path("data").path("jobId").asText();

        JsonNode data = pollUntilTerminal(jobId);
        assertThat(data.path("status").asText()).isEqualTo("DONE");
        assertThat(data.path("answers")).hasSize(2);
        assertThat(data.path("answers").get(0).path("id").asText()).isEqualTo("q1");
        assertThat(data.path("answers").get(0).path("answer").asText()).isNotBlank();
        assertThat(data.path("answers").get(1).path("id").asText()).isEqualTo("q2");
    }

    @Test
    void rejectsRequestWithoutApiKey() throws Exception {
        mockMvc.perform(multipart("/api/v1/answers")
                        .file(jsonPart())
                        .file(pdfPart("cv", "CV de prueba")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    @Test
    void unknownAnswerJobReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/answers/{id}", "does-not-exist").header("X-Api-Key", API_KEY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("JOB_NOT_FOUND"));
    }

    private MockMultipartFile jsonPart() throws Exception {
        AnswerBatchRequest request = new AnswerBatchRequest(
                List.of(
                        new AnswerQuestionRequest("q1", "¿Por qué quieres este puesto?", "textarea", 200, null),
                        new AnswerQuestionRequest("q2", "Años de experiencia en Java", "text", 10, null)),
                null,
                "es");
        return new MockMultipartFile("request", "request", "application/json",
                objectMapper.writeValueAsBytes(request));
    }

    private MockMultipartFile pdfPart(String name, String text) throws Exception {
        return new MockMultipartFile(name, name + ".pdf", "application/pdf", PdfTestFactory.pdf(text));
    }

    private JsonNode pollUntilTerminal(String jobId) throws Exception {
        JsonNode data = null;
        for (int attempt = 0; attempt < 100; attempt++) {
            MvcResult result = mockMvc.perform(get("/api/v1/answers/{id}", jobId).header("X-Api-Key", API_KEY))
                    .andExpect(status().isOk())
                    .andReturn();
            data = readJson(result).path("data");
            String status = data.path("status").asText();
            if ("DONE".equals(status) || "ERROR".equals(status)) {
                return data;
            }
            Thread.sleep(50);
        }
        return data;
    }

    private JsonNode readJson(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }
}
