package com.jobmatch.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobmatch.ai.api.dto.JobOfferRequest;
import com.jobmatch.ai.support.PdfTestFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(properties = "ai.mode=stub")
@AutoConfigureMockMvc
class AnalysisFlowIntegrationTest {

    private static final String API_KEY = "test-key";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void healthEndpointReportsUp() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("UP"));
    }

    @Test
    void fullFlowProducesAnalysis() throws Exception {
        MvcResult created = mockMvc.perform(multipart("/api/v1/analyses")
                        .file(jsonPart())
                        .file(pdfPart("cv", "CV: Java, Spring, 5 años de experiencia"))
                        .file(pdfPart("coverLetter", "Carta: candidato motivado con experiencia"))
                        .header("X-Api-Key", API_KEY))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.jobId").isNotEmpty())
                .andReturn();

        String jobId = readJson(created).path("data").path("jobId").asText();

        JsonNode data = pollUntilTerminal(jobId);
        assertThat(data.path("status").asText()).isEqualTo("DONE");
        assertThat(data.path("result").path("cvScore").asInt()).isBetween(0, 100);
        assertThat(data.path("result").path("globalMatch").asInt()).isBetween(0, 100);
        assertThat(data.path("result").path("improvedCv").asText()).isNotBlank();
    }

    @Test
    void rejectsRequestWithoutApiKey() throws Exception {
        mockMvc.perform(multipart("/api/v1/analyses")
                        .file(jsonPart())
                        .file(pdfPart("cv", "CV"))
                        .file(pdfPart("coverLetter", "Carta")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    @Test
    void unknownJobReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/analyses/{id}", "does-not-exist").header("X-Api-Key", API_KEY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("JOB_NOT_FOUND"));
    }

    private MockMultipartFile jsonPart() throws Exception {
        JobOfferRequest offer = new JobOfferRequest(
                "Backend Engineer", "Acme",
                "Buscamos ingeniero backend con Java y Spring Boot", "Madrid", null, "es");
        return new MockMultipartFile("request", "request", "application/json",
                objectMapper.writeValueAsBytes(offer));
    }

    private MockMultipartFile pdfPart(String name, String text) throws Exception {
        return new MockMultipartFile(name, name + ".pdf", "application/pdf", PdfTestFactory.pdf(text));
    }

    private JsonNode pollUntilTerminal(String jobId) throws Exception {
        JsonNode data = null;
        for (int attempt = 0; attempt < 100; attempt++) {
            MvcResult result = mockMvc.perform(get("/api/v1/analyses/{id}", jobId).header("X-Api-Key", API_KEY))
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
