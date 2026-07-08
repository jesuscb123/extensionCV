package com.jobmatch.ai.api;

import com.jobmatch.ai.answer.QuestionAnswerService;
import com.jobmatch.ai.api.dto.AnswerBatchRequest;
import com.jobmatch.ai.api.dto.AnswerJobResponse;
import com.jobmatch.ai.api.dto.ApiResponse;
import com.jobmatch.ai.api.dto.JobCreatedResponse;
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

/** Modo "responder preguntas": genera respuestas sugeridas para las preguntas de un formulario de candidatura. */
@RestController
@RequestMapping("/api/v1/answers")
public class AnswerController {

    private final QuestionAnswerService answerService;
    private final MultipartPdfValidator pdfValidator;

    public AnswerController(QuestionAnswerService answerService, MultipartPdfValidator pdfValidator) {
        this.answerService = answerService;
        this.pdfValidator = pdfValidator;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<JobCreatedResponse>> create(
            @RequestPart("request") @Valid AnswerBatchRequest request,
            @RequestPart("cv") MultipartFile cv) {

        pdfValidator.validate(cv, "cv");

        JobCreatedResponse created = answerService.createJob(request, pdfValidator.readBytes(cv, "cv"));

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ApiResponse.ok(created));
    }

    @GetMapping("/{jobId}")
    public ApiResponse<AnswerJobResponse> get(@PathVariable String jobId) {
        return ApiResponse.ok(answerService.getJob(jobId));
    }
}
