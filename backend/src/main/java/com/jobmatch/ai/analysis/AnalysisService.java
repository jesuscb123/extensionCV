package com.jobmatch.ai.analysis;

import com.jobmatch.ai.api.dto.AnalysisCreatedResponse;
import com.jobmatch.ai.api.dto.AnalysisJobResponse;
import com.jobmatch.ai.api.dto.JobOfferRequest;
import com.jobmatch.ai.exception.JobNotFoundException;
import com.jobmatch.ai.job.JobStore;
import java.util.UUID;
import org.springframework.stereotype.Service;

/** Orquesta el caso de uso: crea el job, lanza el procesado async y consulta el estado. */
@Service
public class AnalysisService {

    private final JobStore jobStore;
    private final AnalysisProcessor processor;

    public AnalysisService(JobStore jobStore, AnalysisProcessor processor) {
        this.jobStore = jobStore;
        this.processor = processor;
    }

    public AnalysisCreatedResponse createAnalysis(JobOfferRequest offer, byte[] cvBytes, byte[] coverLetterBytes) {
        String jobId = UUID.randomUUID().toString();
        AnalysisJob job = new AnalysisJob(jobId);
        jobStore.save(job);

        processor.process(jobId, offer, cvBytes, coverLetterBytes);

        return new AnalysisCreatedResponse(jobId, job.status());
    }

    public AnalysisJobResponse getJob(String jobId) {
        AnalysisJob job = jobStore.find(jobId).orElseThrow(() -> new JobNotFoundException(jobId));
        return new AnalysisJobResponse(job.id(), job.status(), job.result());
    }
}
