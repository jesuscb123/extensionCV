package com.jobmatch.ai.api;

import com.jobmatch.ai.analysis.AnalysisService;
import com.jobmatch.ai.api.dto.AnalysisJobResponse;
import com.jobmatch.ai.api.dto.ApiResponse;
import com.jobmatch.ai.api.dto.JobCreatedResponse;
import com.jobmatch.ai.api.dto.JobOfferRequest;
import com.jobmatch.ai.pdf.MultipartPdfValidator;
import jakarta.validation.Valid;
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

    private final AnalysisService analysisService;
    private final MultipartPdfValidator pdfValidator;

    public AnalysisController(AnalysisService analysisService, MultipartPdfValidator pdfValidator) {
        this.analysisService = analysisService;
        this.pdfValidator = pdfValidator;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<JobCreatedResponse>> create(
            @RequestPart("request") @Valid JobOfferRequest request,
            @RequestPart("cv") MultipartFile cv,
            @RequestPart("coverLetter") MultipartFile coverLetter) {

        pdfValidator.validate(cv, "cv");
        pdfValidator.validate(coverLetter, "coverLetter");

        JobCreatedResponse created = analysisService.createAnalysis(
                request, pdfValidator.readBytes(cv, "cv"), pdfValidator.readBytes(coverLetter, "coverLetter"));

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ApiResponse.ok(created));
    }

    @GetMapping("/{jobId}")
    public ApiResponse<AnalysisJobResponse> get(@PathVariable String jobId) {
        return ApiResponse.ok(analysisService.getJob(jobId));
    }
}
