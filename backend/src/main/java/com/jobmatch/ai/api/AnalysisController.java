package com.jobmatch.ai.api;

import com.jobmatch.ai.analysis.AnalysisService;
import com.jobmatch.ai.api.dto.AnalysisCreatedResponse;
import com.jobmatch.ai.api.dto.AnalysisJobResponse;
import com.jobmatch.ai.api.dto.ApiResponse;
import com.jobmatch.ai.api.dto.ErrorCode;
import com.jobmatch.ai.api.dto.JobOfferRequest;
import com.jobmatch.ai.exception.ApiException;
import jakarta.validation.Valid;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/analyses")
public class AnalysisController {

    private static final String PDF_CONTENT_TYPE = "application/pdf";

    private final AnalysisService analysisService;

    public AnalysisController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<AnalysisCreatedResponse>> create(
            @RequestPart("request") @Valid JobOfferRequest request,
            @RequestPart("cv") MultipartFile cv,
            @RequestPart("coverLetter") MultipartFile coverLetter) {

        validatePdf(cv, "cv");
        validatePdf(coverLetter, "coverLetter");

        AnalysisCreatedResponse created = analysisService.createAnalysis(
                request, readBytes(cv, "cv"), readBytes(coverLetter, "coverLetter"));

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ApiResponse.ok(created));
    }

    @GetMapping("/{jobId}")
    public ApiResponse<AnalysisJobResponse> get(@PathVariable String jobId) {
        return ApiResponse.ok(analysisService.getJob(jobId));
    }

    private void validatePdf(MultipartFile file, String field) {
        if (file == null || file.isEmpty()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "El archivo '" + field + "' es obligatorio");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase().startsWith(PDF_CONTENT_TYPE)) {
            throw new ApiException(ErrorCode.UNSUPPORTED_MEDIA_TYPE, "El archivo '" + field + "' debe ser un PDF");
        }
    }

    private byte[] readBytes(MultipartFile file, String field) {
        try {
            return file.getBytes();
        } catch (IOException ex) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "No se pudo leer el archivo '" + field + "'");
        }
    }
}
