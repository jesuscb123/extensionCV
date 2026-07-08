package com.jobmatch.ai.exception;

/** El PDF está vacío, corrupto o no contiene texto extraíble. */
public class PdfExtractionException extends RuntimeException {

    public PdfExtractionException(String message) {
        super(message);
    }

    public PdfExtractionException(String message, Throwable cause) {
        super(message, cause);
    }
}
