package com.jobmatch.ai.api.dto;

/** Metadatos del CV almacenado: nombre, fecha de subida (ISO-8601) y tamaño en bytes. */
public record CvMetadataResponse(String fileName, String storedAt, long sizeBytes) {
}
