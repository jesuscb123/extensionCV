package com.jobmatch.ai.job;

/** Estado de un trabajo asíncrono, compartido por cualquier feature (análisis, respuestas, ...). */
public enum JobStatus {
    PENDING,
    PROCESSING,
    DONE,
    ERROR
}
