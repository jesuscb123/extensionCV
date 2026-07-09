package com.jobmatch.ai.cv;

import java.time.Instant;

/** Metadatos del CV actualmente almacenado (solo se conserva la última versión subida). */
public record StoredCvMetadata(String fileName, Instant storedAt, long sizeBytes) {
}
