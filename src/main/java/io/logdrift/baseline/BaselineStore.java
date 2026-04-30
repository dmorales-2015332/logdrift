package io.logdrift.baseline;

import io.logdrift.schema.LogSchema;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Persists and retrieves baseline schemas for microservices.
 * Baselines are stored as JSON files in a configurable directory.
 */
public class BaselineStore {

    private final Path storeDirectory;
    private final ObjectMapper objectMapper;

    public BaselineStore(Path storeDirectory) throws IOException {
        this.storeDirectory = storeDirectory;
        this.objectMapper = new ObjectMapper();
        Files.createDirectories(storeDirectory);
    }

    public void saveBaseline(String serviceName, LogSchema schema) throws IOException {
        if (serviceName == null || serviceName.isBlank()) {
            throw new IllegalArgumentException("Service name must not be blank");
        }
        File file = resolveFile(serviceName);
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, schema);
    }

    public Optional<LogSchema> loadBaseline(String serviceName) throws IOException {
        File file = resolveFile(serviceName);
        if (!file.exists()) {
            return Optional.empty();
        }
        return Optional.of(objectMapper.readValue(file, LogSchema.class));
    }

    public boolean deleteBaseline(String serviceName) {
        File file = resolveFile(serviceName);
        return file.exists() && file.delete();
    }

    public Map<String, LogSchema> loadAllBaselines() throws IOException {
        Map<String, LogSchema> baselines = new HashMap<>();
        File[] files = storeDirectory.toFile().listFiles(
            f -> f.isFile() && f.getName().endsWith(".json")
        );
        if (files != null) {
            for (File file : files) {
                String name = file.getName().replace(".json", "");
                baselines.put(name, objectMapper.readValue(file, LogSchema.class));
            }
        }
        return baselines;
    }

    private File resolveFile(String serviceName) {
        String sanitized = serviceName.replaceAll("[^a-zA-Z0-9_\\-]", "_");
        return storeDirectory.resolve(sanitized + ".json").toFile();
    }
}
