package com.jobmatch.ai.api.dto;

/** Detalle de un error de validación asociado a un campo concreto. */
public record ErrorDetail(String field, String issue) {
}
