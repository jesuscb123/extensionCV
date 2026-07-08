package com.jobmatch.ai.analysis;

import com.jobmatch.ai.api.dto.AnalysisResultDto;
import com.jobmatch.ai.job.JobStatus;
import com.jobmatch.ai.job.TimestampedJob;
import java.time.Instant;

/** Estado mutable de un análisis en el {@code JobStore}. */
public class AnalysisJob implements TimestampedJob {

    private final String id;
    private final Instant createdAt;
    private volatile JobStatus status;
    private volatile AnalysisResultDto result;
    private volatile String error;

    public AnalysisJob(String id) {
        this.id = id;
        this.createdAt = Instant.now();
        this.status = JobStatus.PENDING;
    }

    public String id() {
        return id;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public JobStatus status() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }

    public AnalysisResultDto result() {
        return result;
    }

    public void setResult(AnalysisResultDto result) {
        this.result = result;
    }

    public String error() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
