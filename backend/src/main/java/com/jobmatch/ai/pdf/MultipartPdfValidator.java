package com.jobmatch.ai.pdf;

import com.jobmatch.ai.api.dto.ErrorCode;
import com.jobmatch.ai.exception.ApiException;
import java.io.IOException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/** Valida y lee los ficheros PDF recibidos por multipart, compartido por los distintos controllers. */
@Component
public class MultipartPdfValidator {

    private static final String PDF_CONTENT_TYPE = "application/pdf";

    public void validate(MultipartFile file, String field) {
        if (file == null || file.isEmpty()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "El archivo '" + field + "' es obligatorio");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase().startsWith(PDF_CONTENT_TYPE)) {
            throw new ApiException(ErrorCode.UNSUPPORTED_MEDIA_TYPE, "El archivo '" + field + "' debe ser un PDF");
        }
    }

    public byte[] readBytes(MultipartFile file, String field) {
        try {
            return file.getBytes();
        } catch (IOException ex) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "No se pudo leer el archivo '" + field + "'");
        }
    }
}
