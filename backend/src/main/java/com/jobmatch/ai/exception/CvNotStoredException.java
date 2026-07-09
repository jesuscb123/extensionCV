package com.jobmatch.ai.exception;

/** No hay ningún CV almacenado todavía. */
public class CvNotStoredException extends RuntimeException {

    public CvNotStoredException() {
        super("No hay ningún CV almacenado todavía");
    }
}
