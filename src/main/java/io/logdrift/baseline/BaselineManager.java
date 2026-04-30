package io.logdrift.baseline;

import io.logdrift.schema.LogSchema;
import io.logdrift.schema.LogSchemaExtractor;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * High-level facade for managing baseline schemas.
 * Coordinates extraction and persistence of baselines.
 */
public class BaselineManager {

    private final BaselineStore store;
    private final LogSchemaExtractor extractor;

    public BaselineManager(Path storeDirectory) throws IOException {
        this.store = new BaselineStore(storeDirectory);
        this.extractor = new LogSchemaExtractor();
    }

    public BaselineManager(BaselineStore store, LogSchemaExtractor extractor) {
        this.store = store;
        this.extractor = extractor;
    }

    /**
     * Extracts a schema from the provided log lines and saves it as the baseline
     * for the given service.
     */
    public LogSchema captureBaseline(String serviceName, List<String> logLines) throws IOException {
        if (logLines == null || logLines.isEmpty()) {
            throw new IllegalArgumentException("Log lines must not be empty");
        }
        LogSchema schema = extractor.extract(logLines);
        store.saveBaseline(serviceName, schema);
        return schema;
    }

    public Optional<LogSchema> getBaseline(String serviceName) throws IOException {
        return store.loadBaseline(serviceName);
    }

    public Map<String, LogSchema> getAllBaselines() throws IOException {
        return store.loadAllBaselines();
    }

    public boolean removeBaseline(String serviceName) {
        return store.deleteBaseline(serviceName);
    }

    public boolean hasBaseline(String serviceName) throws IOException {
        return store.loadBaseline(serviceName).isPresent();
    }
}
