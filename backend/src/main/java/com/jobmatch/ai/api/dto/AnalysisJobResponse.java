package com.jobmatch.ai.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.jobmatch.ai.job.JobStatus;

/** Vista del job: {@code GET /api/v1/analyses/{jobId}}. El resultado se omite si aún no está listo. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AnalysisJobResponse(String jobId, JobStatus status, AnalysisResultDto result) {
}
