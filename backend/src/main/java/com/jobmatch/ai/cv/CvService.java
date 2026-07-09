package com.jobmatch.ai.cv;

import com.jobmatch.ai.api.dto.CvMetadataResponse;
import java.util.Optional;
import org.springframework.stereotype.Service;

/** Orquesta el almacenamiento del CV: guardar, consultar metadatos/contenido y eliminar. */
@Service
public class CvService {

    private final CvStorage storage;

    public CvService(CvStorage storage) {
        this.storage = storage;
    }

    public CvMetadataResponse store(String fileName, byte[] content) {
        return toResponse(storage.save(fileName, content));
    }

    public Optional<CvMetadataResponse> getMetadata() {
        return storage.currentMetadata().map(this::toResponse);
    }

    public Optional<byte[]> getContent() {
        return storage.currentContent();
    }

    public void delete() {
        storage.delete();
    }

    private CvMetadataResponse toResponse(StoredCvMetadata metadata) {
        return new CvMetadataResponse(metadata.fileName(), metadata.storedAt().toString(), metadata.sizeBytes());
    }
}
