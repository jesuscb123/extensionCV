package com.jobmatch.ai.api.dto;

import com.jobmatch.ai.job.JobStatus;

/** Respuesta de la creación de un job asíncrono (análisis o respuestas): {@code 202 { jobId, status }}. */
public record JobCreatedResponse(String jobId, JobStatus status) {
}
