package com.jobmatch.ai.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Datos de la oferta de empleo enviados por la extensión (parte JSON del multipart). */
public record JobOfferRequest(
        @NotBlank @Size(max = 300) String title,
        @NotBlank @Size(max = 300) String company,
        @NotBlank @Size(max = 30_000) String description,
        @Size(max = 300) String location,
        @Size(max = 2_000) String sourceUrl,
        @Size(max = 20) String language) {
}
