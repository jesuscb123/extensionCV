package com.jobmatch.ai.analysis;

import com.jobmatch.ai.job.InMemoryJobStore;
import org.springframework.stereotype.Component;

/** Bean concreto de {@link InMemoryJobStore} para trabajos de análisis (tipo de bean distinguible). */
@Component
public class AnalysisJobStore extends InMemoryJobStore<AnalysisJob> {
}
