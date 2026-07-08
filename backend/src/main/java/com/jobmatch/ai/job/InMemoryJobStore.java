package com.jobmatch.ai.job;

import com.jobmatch.ai.analysis.AnalysisJob;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Component;

/**
 * Almacén en memoria con expiración por TTL. Suficiente para el MVP; sustituible por
 * Redis en versiones futuras sin cambiar el resto del código (depende de {@link JobStore}).
 */
@Component
public class InMemoryJobStore implements JobStore {

    private static final Duration TTL = Duration.ofMinutes(30);

    private final ConcurrentMap<String, AnalysisJob> jobs = new ConcurrentHashMap<>();

    @Override
    public void save(AnalysisJob job) {
        jobs.put(job.id(), job);
        evictExpired();
    }

    @Override
    public Optional<AnalysisJob> find(String jobId) {
        return Optional.ofNullable(jobs.get(jobId));
    }

    private void evictExpired() {
        Instant threshold = Instant.now().minus(TTL);
        jobs.values().removeIf(job -> job.createdAt().isBefore(threshold));
    }
}
