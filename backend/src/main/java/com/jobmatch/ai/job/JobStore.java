package com.jobmatch.ai.job;

import com.jobmatch.ai.analysis.AnalysisJob;
import java.util.Optional;

/** Almacén de trabajos de análisis asíncronos. */
public interface JobStore {

    void save(AnalysisJob job);

    Optional<AnalysisJob> find(String jobId);
}
