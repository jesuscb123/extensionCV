package com.jobmatch.ai.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/** Envoltorio para deserializar el JSON de la IA: {@code { "answers": [ {id, answer}, ... ] } }. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AiAnswerBatch(List<AiAnswer> answers) {
}
