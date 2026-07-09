package com.jobmatch.ai.cv;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobmatch.ai.config.StorageProperties;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Almacena el CV en el sistema de ficheros local: el PDF y sus metadatos (nombre,
 * fecha, tamaño) en un JSON junto a él. Solo se conserva la última versión subida;
 * cada nueva subida sustituye a la anterior (minimización de datos personales).
 */
@Component
public class FileSystemCvStorage implements CvStorage {

    private static final String PDF_FILE = "current.pdf";
    private static final String METADATA_FILE = "metadata.json";

    private final Path pdfPath;
    private final Path metadataPath;
    private final ObjectMapper objectMapper;
    private final Object lock = new Object();

    public FileSystemCvStorage(StorageProperties properties, ObjectMapper objectMapper) {
        Path cvDir = Path.of(properties.directory(), "cv");
        this.pdfPath = cvDir.resolve(PDF_FILE);
        this.metadataPath = cvDir.resolve(METADATA_FILE);
        this.objectMapper = objectMapper;
        createDirectories(cvDir);
    }

    @Override
    public StoredCvMetadata save(String fileName, byte[] content) {
        synchronized (lock) {
            StoredCvMetadata metadata = new StoredCvMetadata(fileName, Instant.now(), content.length);
            try {
                Files.write(pdfPath, content);
                objectMapper.writeValue(metadataPath.toFile(), metadata);
            } catch (IOException ex) {
                throw new UncheckedIOException("No se pudo guardar el CV", ex);
            }
            return metadata;
        }
    }

    @Override
    public Optional<StoredCvMetadata> currentMetadata() {
        synchronized (lock) {
            if (!Files.exists(metadataPath)) {
                return Optional.empty();
            }
            try {
                return Optional.of(objectMapper.readValue(metadataPath.toFile(), StoredCvMetadata.class));
            } catch (IOException ex) {
                throw new UncheckedIOException("No se pudo leer los metadatos del CV", ex);
            }
        }
    }

    @Override
    public Optional<byte[]> currentContent() {
        synchronized (lock) {
            if (!Files.exists(pdfPath)) {
                return Optional.empty();
            }
            try {
                return Optional.of(Files.readAllBytes(pdfPath));
            } catch (IOException ex) {
                throw new UncheckedIOException("No se pudo leer el CV almacenado", ex);
            }
        }
    }

    @Override
    public void delete() {
        synchronized (lock) {
            try {
                Files.deleteIfExists(pdfPath);
                Files.deleteIfExists(metadataPath);
            } catch (IOException ex) {
                throw new UncheckedIOException("No se pudo eliminar el CV almacenado", ex);
            }
        }
    }

    private static void createDirectories(Path dir) {
        try {
            Files.createDirectories(dir);
        } catch (IOException ex) {
            throw new UncheckedIOException("No se pudo crear el directorio de almacenamiento", ex);
        }
    }
}
