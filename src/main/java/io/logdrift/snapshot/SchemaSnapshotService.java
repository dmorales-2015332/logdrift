package io.logdrift.snapshot;

import io.logdrift.schema.LogSchema;
import io.logdrift.schema.LogSchemaExtractor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Manages capturing, storing, and retrieving {@link SchemaSnapshot} instances.
 */
public class SchemaSnapshotService {

    private final LogSchemaExtractor extractor;
    // key: serviceId -> ordered list of snapshots
    private final Map<String, List<SchemaSnapshot>> snapshotStore = new ConcurrentHashMap<>();

    public SchemaSnapshotService(LogSchemaExtractor extractor) {
        this.extractor = Objects.requireNonNull(extractor, "extractor must not be null");
    }

    /**
     * Captures a snapshot from raw log lines for the given service and version.
     */
    public SchemaSnapshot capture(String serviceId, String version, List<String> logLines) {
        LogSchema schema = extractor.extract(logLines);
        SchemaSnapshot snapshot = new SchemaSnapshot(serviceId, version, schema, Instant.now());
        snapshotStore.computeIfAbsent(serviceId, k -> Collections.synchronizedList(new ArrayList<>()))
                     .add(snapshot);
        return snapshot;
    }

    /**
     * Returns all snapshots for a given service, ordered by capture time ascending.
     */
    public List<SchemaSnapshot> getSnapshots(String serviceId) {
        return snapshotStore.getOrDefault(serviceId, Collections.emptyList())
                .stream()
                .sorted((a, b) -> a.getCapturedAt().compareTo(b.getCapturedAt()))
                .collect(Collectors.toList());
    }

    /**
     * Returns the most recent snapshot for a given service, or null if none exists.
     */
    public SchemaSnapshot getLatest(String serviceId) {
        List<SchemaSnapshot> snapshots = getSnapshots(serviceId);
        return snapshots.isEmpty() ? null : snapshots.get(snapshots.size() - 1);
    }

    /**
     * Removes all snapshots for a given service.
     */
    public void clearSnapshots(String serviceId) {
        snapshotStore.remove(serviceId);
    }

    /**
     * Returns the total number of snapshots stored across all services.
     */
    public int totalSnapshotCount() {
        return snapshotStore.values().stream().mapToInt(List::size).sum();
    }
}
