package com.jobmatch.ai.answer;

import com.jobmatch.ai.ai.AiAnswer;
import com.jobmatch.ai.ai.QuestionAnswerAiClient;
import com.jobmatch.ai.api.dto.AnswerBatchRequest;
import com.jobmatch.ai.api.dto.AnswerQuestionRequest;
import com.jobmatch.ai.api.dto.AnsweredQuestionDto;
import com.jobmatch.ai.exception.AiProviderException;
import com.jobmatch.ai.exception.PdfExtractionException;
import com.jobmatch.ai.job.JobStatus;
import com.jobmatch.ai.job.JobStore;
import com.jobmatch.ai.pdf.PdfExtractor;
import com.jobmatch.ai.prompt.QuestionAnswerPromptBuilder;
import com.jobmatch.ai.prompt.QuestionAnswerPromptContext;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/** Ejecuta en segundo plano: PDF → prompt → IA → emparejar respuestas → persistencia del job. */
@Component
public class QuestionAnswerProcessor {

    private static final Logger log = LoggerFactory.getLogger(QuestionAnswerProcessor.class);
    private static final String DEFAULT_LANGUAGE = "es";

    private final PdfExtractor pdfExtractor;
    private final QuestionAnswerPromptBuilder promptBuilder;
    private final QuestionAnswerAiClient aiClient;
    private final JobStore<QuestionAnswerJob> jobStore;

    public QuestionAnswerProcessor(PdfExtractor pdfExtractor,
                                    QuestionAnswerPromptBuilder promptBuilder,
                                    QuestionAnswerAiClient aiClient,
                                    JobStore<QuestionAnswerJob> jobStore) {
        this.pdfExtractor = pdfExtractor;
        this.promptBuilder = promptBuilder;
        this.aiClient = aiClient;
        this.jobStore = jobStore;
    }

    @Async
    public void process(String jobId, AnswerBatchRequest request, byte[] cvBytes) {
        QuestionAnswerJob job = jobStore.find(jobId).orElse(null);
        if (job == null) {
            return;
        }
        job.setStatus(JobStatus.PROCESSING);
        jobStore.save(job);

        try {
            String cvText = pdfExtractor.extract(cvBytes);
            String language = resolveLanguage(request.language());

            QuestionAnswerPromptContext context = new QuestionAnswerPromptContext(
                    cvText, request.questions(), request.jobOffer(), language);

            List<AiAnswer> aiAnswers = aiClient.answer(
                    promptBuilder.system(), promptBuilder.user(context), request.questions());

            job.setResult(zip(request.questions(), aiAnswers));
            job.setStatus(JobStatus.DONE);
            jobStore.save(job);
        } catch (PdfExtractionException | AiProviderException ex) {
            fail(job, ex.getMessage());
        } catch (RuntimeException ex) {
            log.error("Fallo inesperado respondiendo preguntas del job {}", jobId, ex);
            fail(job, "Error inesperado generando las respuestas");
        }
    }

    private List<AnsweredQuestionDto> zip(List<AnswerQuestionRequest> questions, List<AiAnswer> aiAnswers) {
        Map<String, String> answersById = aiAnswers.stream()
                .collect(Collectors.toMap(AiAnswer::id, AiAnswer::answer, (a, b) -> a));

        return questions.stream()
                .map(q -> new AnsweredQuestionDto(q.id(), q.label(), answersById.getOrDefault(q.id(), "")))
                .toList();
    }

    private void fail(QuestionAnswerJob job, String message) {
        job.setStatus(JobStatus.ERROR);
        job.setError(message);
        jobStore.save(job);
    }

    private static String resolveLanguage(String language) {
        return (language == null || language.isBlank()) ? DEFAULT_LANGUAGE : language;
    }
}
