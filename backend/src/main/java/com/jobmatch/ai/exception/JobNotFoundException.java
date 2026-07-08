package com.jobmatch.ai.exception;

/** No existe (o ha expirado) el job solicitado. */
public class JobNotFoundException extends RuntimeException {

    public JobNotFoundException(String jobId) {
        super("No existe el análisis con id: " + jobId);
    }
}
