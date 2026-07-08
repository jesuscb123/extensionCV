package com.jobmatch.ai.prompt;

import com.jobmatch.ai.api.dto.AnswerQuestionRequest;
import com.jobmatch.ai.api.dto.JobContextRequest;
import java.util.List;

/** Datos que consume el {@link QuestionAnswerPromptBuilder} para construir el prompt de respuestas. */
public record QuestionAnswerPromptContext(
        String cvText,
        List<AnswerQuestionRequest> questions,
        JobContextRequest jobOffer,
        String language) {
}
