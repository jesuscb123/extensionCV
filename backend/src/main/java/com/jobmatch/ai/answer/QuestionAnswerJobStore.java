package com.jobmatch.ai.answer;

import com.jobmatch.ai.job.InMemoryJobStore;
import org.springframework.stereotype.Component;

/** Bean concreto de {@link InMemoryJobStore} para jobs de respuestas (tipo de bean distinguible). */
@Component
public class QuestionAnswerJobStore extends InMemoryJobStore<QuestionAnswerJob> {
}
