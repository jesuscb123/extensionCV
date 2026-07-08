package com.jobmatch.ai.pdf;

import com.jobmatch.ai.exception.PdfExtractionException;
import java.io.IOException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

/** Extrae el texto plano de un PDF usando Apache PDFBox. */
@Component
public class PdfExtractor {

    public String extract(byte[] pdfBytes) {
        if (pdfBytes == null || pdfBytes.length == 0) {
            throw new PdfExtractionException("El PDF está vacío");
        }
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            String text = new PDFTextStripper().getText(document);
            if (text == null || text.isBlank()) {
                throw new PdfExtractionException("El PDF no contiene texto extraíble (¿es un escaneo?)");
            }
            return text.strip();
        } catch (IOException ex) {
            throw new PdfExtractionException("No se pudo leer el PDF (¿está corrupto?)", ex);
        }
    }
}
