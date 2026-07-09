package com.jobmatch.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobmatch.ai.support.PdfTestFactory;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(properties = "ai.mode=stub")
@AutoConfigureMockMvc
class CvFlowIntegrationTest {

    private static final String API_KEY = "test-key";

    @TempDir
    static Path storageDir;

    @DynamicPropertySource
    static void overrideStorageDirectory(DynamicPropertyRegistry registry) {
        registry.add("app.storage.directory", storageDir::toString);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void resetStorage() throws Exception {
        // Cada test parte de "sin CV almacenado", con independencia del orden de ejecución.
        mockMvc.perform(delete("/api/v1/cv").header("X-Api-Key", API_KEY));
    }

    @Test
    void metadataIsNullWhenNothingStoredYet() throws Exception {
        mockMvc.perform(get("/api/v1/cv").header("X-Api-Key", API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void downloadWithoutStoredCvReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/cv/file").header("X-Api-Key", API_KEY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("CV_NOT_FOUND"));
    }

    @Test
    void uploadStoresCvAndReturnsMetadataAndContent() throws Exception {
        byte[] cvBytes = PdfTestFactory.pdf("CV de prueba");

        MvcResult uploadResult = mockMvc.perform(multipart("/api/v1/cv")
                        .file(new MockMultipartFile("cv", "mi_cv.pdf", "application/pdf", cvBytes))
                        .header("X-Api-Key", API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fileName").value("mi_cv.pdf"))
                .andExpect(jsonPath("$.data.sizeBytes").value(cvBytes.length))
                .andReturn();

        assertThat(readJson(uploadResult).path("data").path("storedAt").asText()).isNotBlank();

        mockMvc.perform(get("/api/v1/cv").header("X-Api-Key", API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fileName").value("mi_cv.pdf"));

        MvcResult downloadResult = mockMvc.perform(get("/api/v1/cv/file").header("X-Api-Key", API_KEY))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(downloadResult.getResponse().getContentAsByteArray()).isEqualTo(cvBytes);
    }

    @Test
    void secondUploadReplacesThePrevious() throws Exception {
        mockMvc.perform(multipart("/api/v1/cv")
                        .file(new MockMultipartFile("cv", "cv_v1.pdf", "application/pdf", PdfTestFactory.pdf("v1")))
                        .header("X-Api-Key", API_KEY))
                .andExpect(status().isOk());

        byte[] secondCv = PdfTestFactory.pdf("v2, mas reciente");
        mockMvc.perform(multipart("/api/v1/cv")
                        .file(new MockMultipartFile("cv", "cv_v2.pdf", "application/pdf", secondCv))
                        .header("X-Api-Key", API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fileName").value("cv_v2.pdf"));

        mockMvc.perform(get("/api/v1/cv").header("X-Api-Key", API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fileName").value("cv_v2.pdf"));

        MvcResult download = mockMvc.perform(get("/api/v1/cv/file").header("X-Api-Key", API_KEY))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(download.getResponse().getContentAsByteArray()).isEqualTo(secondCv);
    }

    @Test
    void deleteRemovesStoredCv() throws Exception {
        mockMvc.perform(multipart("/api/v1/cv")
                        .file(new MockMultipartFile("cv", "cv.pdf", "application/pdf", PdfTestFactory.pdf("CV")))
                        .header("X-Api-Key", API_KEY))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/v1/cv").header("X-Api-Key", API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.deleted").value(true));

        mockMvc.perform(get("/api/v1/cv").header("X-Api-Key", API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void rejectsRequestWithoutApiKey() throws Exception {
        mockMvc.perform(get("/api/v1/cv"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    @Test
    void rejectsNonPdfUpload() throws Exception {
        mockMvc.perform(multipart("/api/v1/cv")
                        .file(new MockMultipartFile("cv", "cv.txt", "text/plain", "no soy un pdf".getBytes()))
                        .header("X-Api-Key", API_KEY))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.error.code").value("UNSUPPORTED_MEDIA_TYPE"));
    }

    private JsonNode readJson(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }
}
