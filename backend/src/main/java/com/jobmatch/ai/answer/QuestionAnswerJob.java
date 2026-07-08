package com.jobmatch.ai.answer;

import com.jobmatch.ai.api.dto.AnsweredQuestionDto;
import com.jobmatch.ai.job.JobStatus;
import com.jobmatch.ai.job.TimestampedJob;
import java.time.Instant;
import java.util.List;

/** Estado mutable de un job de "responder preguntas" en el {@code JobStore}. */
public class QuestionAnswerJob implements TimestampedJob {

    private final String id;
    private final Instant createdAt;
    private volatile JobStatus status;
    private volatile List<AnsweredQuestionDto> result;
    private volatile String error;

    public QuestionAnswerJob(String id) {
        this.id = id;
        this.createdAt = Instant.now();
        this.status = JobStatus.PENDING;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public Instant createdAt() {
        return createdAt;
    }

    public JobStatus status() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }

    public List<AnsweredQuestionDto> result() {
        return result;
    }

    public void setResult(List<AnsweredQuestionDto> result) {
        this.result = result;
    }

    public String error() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
