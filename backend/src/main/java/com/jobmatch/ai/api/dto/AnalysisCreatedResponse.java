package com.jobmatch.ai.api.dto;

import com.jobmatch.ai.analysis.JobStatus;

/** Respuesta de la creación del job: {@code POST /api/v1/analyses}. */
public record AnalysisCreatedResponse(String jobId, JobStatus status) {
}
