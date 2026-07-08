package com.jobmatch.ai.job;

import java.util.Optional;

/** Almacén de trabajos asíncronos, reutilizable por cualquier feature (análisis, respuestas, ...). */
public interface JobStore<T extends TimestampedJob> {

    void save(T job);

    Optional<T> find(String jobId);
}
