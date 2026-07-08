package com.jobmatch.ai.exception;

/** Fallo al comunicarse con el proveedor de IA o al interpretar su respuesta. */
public class AiProviderException extends RuntimeException {

    public AiProviderException(String message) {
        super(message);
    }

    public AiProviderException(String message, Throwable cause) {
        super(message, cause);
    }
}
