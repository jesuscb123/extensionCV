package com.jobmatch.ai.job;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Almacén en memoria con expiración por TTL. Suficiente para el MVP; sustituible por
 * Redis en versiones futuras sin cambiar el resto del código (depende de {@link JobStore}).
 * Cada feature expone un bean concreto (p. ej. {@code AnalysisJobStore}) que extiende esta
 * clase para obtener un tipo de bean distinguible por Spring.
 */
public class InMemoryJobStore<T extends TimestampedJob> implements JobStore<T> {

    private static final Duration TTL = Duration.ofMinutes(30);

    private final ConcurrentMap<String, T> jobs = new ConcurrentHashMap<>();

    @Override
    public void save(T job) {
        jobs.put(job.id(), job);
        evictExpired();
    }

    @Override
    public Optional<T> find(String jobId) {
        return Optional.ofNullable(jobs.get(jobId));
    }

    private void evictExpired() {
        Instant threshold = Instant.now().minus(TTL);
        jobs.values().removeIf(job -> job.createdAt().isBefore(threshold));
    }
}
