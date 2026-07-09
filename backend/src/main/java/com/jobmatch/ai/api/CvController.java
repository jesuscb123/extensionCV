package com.jobmatch.ai.api;

import com.jobmatch.ai.api.dto.ApiResponse;
import com.jobmatch.ai.api.dto.CvMetadataResponse;
import com.jobmatch.ai.cv.CvService;
import com.jobmatch.ai.exception.CvNotStoredException;
import com.jobmatch.ai.pdf.MultipartPdfValidator;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Almacena el CV del usuario para no tener que volver a subirlo en cada análisis
 * o generación de respuestas. Solo se conserva la última versión subida.
 */
@RestController
@RequestMapping("/api/v1/cv")
public class CvController {

    private static final String DEFAULT_FILE_NAME = "cv.pdf";

    private final CvService cvService;
    private final MultipartPdfValidator pdfValidator;

    public CvController(CvService cvService, MultipartPdfValidator pdfValidator) {
        this.cvService = cvService;
        this.pdfValidator = pdfValidator;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<CvMetadataResponse> upload(@RequestPart("cv") MultipartFile cv) {
        pdfValidator.validate(cv, "cv");
        byte[] content = pdfValidator.readBytes(cv, "cv");
        String fileName = cv.getOriginalFilename() != null ? cv.getOriginalFilename() : DEFAULT_FILE_NAME;
        return ApiResponse.ok(cvService.store(fileName, content));
    }

    @GetMapping
    public ApiResponse<CvMetadataResponse> metadata() {
        return ApiResponse.ok(cvService.getMetadata().orElse(null));
    }

    @GetMapping(value = "/file", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> file() {
        byte[] content = cvService.getContent().orElseThrow(CvNotStoredException::new);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + DEFAULT_FILE_NAME + "\"")
                .body(content);
    }

    @DeleteMapping
    public ApiResponse<Map<String, Boolean>> delete() {
        cvService.delete();
        return ApiResponse.ok(Map.of("deleted", true));
    }
}
