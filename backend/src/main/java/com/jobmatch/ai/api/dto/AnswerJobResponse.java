package com.jobmatch.ai.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.jobmatch.ai.job.JobStatus;
import java.util.List;

/** Vista del job: {@code GET /api/v1/answers/{jobId}}. Las respuestas se omiten si aún no están listas. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AnswerJobResponse(String jobId, JobStatus status, List<AnsweredQuestionDto> answers) {
}
