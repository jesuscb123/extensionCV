package com.jobmatch.ai.pdf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.jobmatch.ai.exception.PdfExtractionException;
import com.jobmatch.ai.support.PdfTestFactory;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class PdfExtractorTest {

    private final PdfExtractor extractor = new PdfExtractor();

    @Test
    void extractsTextFromValidPdf() throws Exception {
        byte[] pdf = PdfTestFactory.pdf("Ingeniero de software con experiencia en Java");

        String text = extractor.extract(pdf);

        assertThat(text).contains("Ingeniero de software");
    }

    @Test
    void rejectsEmptyInput() {
        assertThatThrownBy(() -> extractor.extract(new byte[0]))
                .isInstanceOf(PdfExtractionException.class);
    }

    @Test
    void rejectsCorruptPdf() {
        byte[] notAPdf = "esto no es un PDF".getBytes(StandardCharsets.UTF_8);

        assertThatThrownBy(() -> extractor.extract(notAPdf))
                .isInstanceOf(PdfExtractionException.class);
    }
}
