package com.jobmatch.ai.analysis;

import com.jobmatch.ai.ai.AiAnalysis;
import com.jobmatch.ai.ai.AiClient;
import com.jobmatch.ai.api.dto.AnalysisResultDto;
import com.jobmatch.ai.api.dto.JobOfferRequest;
import com.jobmatch.ai.config.AiProperties;
import com.jobmatch.ai.exception.AiProviderException;
import com.jobmatch.ai.exception.PdfExtractionException;
import com.jobmatch.ai.job.JobStore;
import com.jobmatch.ai.mapper.AnalysisMapper;
import com.jobmatch.ai.pdf.PdfExtractor;
import com.jobmatch.ai.prompt.PromptBuilder;
import com.jobmatch.ai.prompt.PromptContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/** Ejecuta el análisis en segundo plano: PDF → prompt → IA → mapeo → persistencia del job. */
@Component
public class AnalysisProcessor {

    private static final Logger log = LoggerFactory.getLogger(AnalysisProcessor.class);
    private static final String DEFAULT_LANGUAGE = "es";

    private final PdfExtractor pdfExtractor;
    private final PromptBuilder promptBuilder;
    private final AiClient aiClient;
    private final AnalysisMapper analysisMapper;
    private final JobStore jobStore;
    private final AiProperties aiProperties;

    public AnalysisProcessor(PdfExtractor pdfExtractor,
                             PromptBuilder promptBuilder,
                             AiClient aiClient,
                             AnalysisMapper analysisMapper,
                             JobStore jobStore,
                             AiProperties aiProperties) {
        this.pdfExtractor = pdfExtractor;
        this.promptBuilder = promptBuilder;
        this.aiClient = aiClient;
        this.analysisMapper = analysisMapper;
        this.jobStore = jobStore;
        this.aiProperties = aiProperties;
    }

    @Async
    public void process(String jobId, JobOfferRequest offer, byte[] cvBytes, byte[] coverLetterBytes) {
        AnalysisJob job = jobStore.find(jobId).orElse(null);
        if (job == null) {
            return;
        }
        job.setStatus(JobStatus.PROCESSING);
        jobStore.save(job);

        try {
            String cvText = pdfExtractor.extract(cvBytes);
            String coverLetterText = pdfExtractor.extract(coverLetterBytes);
            String language = resolveLanguage(offer.language());

            PromptContext context = new PromptContext(
                    offer.title(), offer.company(), offer.location(),
                    offer.description(), cvText, coverLetterText, language);

            AiAnalysis aiAnalysis = aiClient.analyze(promptBuilder.system(), promptBuilder.user(context));
            AnalysisResultDto result = analysisMapper.toResult(aiAnalysis, aiProperties.model(), language);

            job.setResult(result);
            job.setStatus(JobStatus.DONE);
            jobStore.save(job);
        } catch (PdfExtractionException | AiProviderException ex) {
            fail(job, ex.getMessage());
        } catch (RuntimeException ex) {
            log.error("Fallo inesperado analizando el job {}", jobId, ex);
            fail(job, "Error inesperado durante el análisis");
        }
    }

    private void fail(AnalysisJob job, String message) {
        job.setStatus(JobStatus.ERROR);
        job.setError(message);
        jobStore.save(job);
    }

    private static String resolveLanguage(String language) {
        return (language == null || language.isBlank()) ? DEFAULT_LANGUAGE : language;
    }
}
