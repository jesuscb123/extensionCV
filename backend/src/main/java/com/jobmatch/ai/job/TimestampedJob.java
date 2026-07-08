package com.jobmatch.ai.job;

import java.time.Instant;

/** Contrato mínimo que necesita {@link InMemoryJobStore} para almacenar y expirar un trabajo. */
public interface TimestampedJob {

    String id();

    Instant createdAt();
}
