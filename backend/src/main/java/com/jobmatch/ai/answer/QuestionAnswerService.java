package com.jobmatch.ai.answer;

import com.jobmatch.ai.api.dto.AnswerBatchRequest;
import com.jobmatch.ai.api.dto.AnswerJobResponse;
import com.jobmatch.ai.api.dto.JobCreatedResponse;
import com.jobmatch.ai.exception.JobNotFoundException;
import com.jobmatch.ai.job.JobStore;
import java.util.UUID;
import org.springframework.stereotype.Service;

/** Orquesta el caso de uso "responder preguntas": crea el job, lanza el procesado async y consulta el estado. */
@Service
public class QuestionAnswerService {

    private final JobStore<QuestionAnswerJob> jobStore;
    private final QuestionAnswerProcessor processor;

    public QuestionAnswerService(JobStore<QuestionAnswerJob> jobStore, QuestionAnswerProcessor processor) {
        this.jobStore = jobStore;
        this.processor = processor;
    }

    public JobCreatedResponse createJob(AnswerBatchRequest request, byte[] cvBytes) {
        String jobId = UUID.randomUUID().toString();
        QuestionAnswerJob job = new QuestionAnswerJob(jobId);
        jobStore.save(job);

        processor.process(jobId, request, cvBytes);

        return new JobCreatedResponse(jobId, job.status());
    }

    public AnswerJobResponse getJob(String jobId) {
        QuestionAnswerJob job = jobStore.find(jobId).orElseThrow(() -> new JobNotFoundException(jobId));
        return new AnswerJobResponse(job.id(), job.status(), job.result());
    }
}
