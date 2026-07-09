package com.jobmatch.ai.cv;

import java.util.Optional;

/** Almacén del CV del usuario. Solo conserva la última versión subida. */
public interface CvStorage {

    StoredCvMetadata save(String fileName, byte[] content);

    Optional<StoredCvMetadata> currentMetadata();

    Optional<byte[]> currentContent();

    void delete();
}
