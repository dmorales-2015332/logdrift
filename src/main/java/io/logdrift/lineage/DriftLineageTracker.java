package io.logdrift.lineage;

import io.logdrift.drift.DriftEvent;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks the lineage of drift events, recording how a schema field
 * evolved across successive deployments or snapshots.
 */
public class DriftLineageTracker {

    private final Map<String, List<DriftLineageEntry>> lineageMap = new ConcurrentHashMap<>();

    /**
     * Records a drift event into the lineage chain for the affected field.
     *
     * @param event the drift event to record
     */
    public void record(DriftEvent event) {
        Objects.requireNonNull(event, "event must not be null");
        String key = buildKey(event.getServiceName(), event.getFieldName());
        DriftLineageEntry entry = new DriftLineageEntry(
                event.getServiceName(),
                event.getFieldName(),
                event.getDriftType(),
                event.getTimestamp() != null ? event.getTimestamp() : Instant.now()
        );
        lineageMap.computeIfAbsent(key, k -> new ArrayList<>()).add(entry);
    }

    /**
     * Returns the full lineage history for a given service and field.
     *
     * @param serviceName the microservice name
     * @param fieldName   the schema field name
     * @return ordered list of lineage entries, oldest first
     */
    public List<DriftLineageEntry> getLineage(String serviceName, String fieldName) {
        String key = buildKey(serviceName, fieldName);
        return Collections.unmodifiableList(
                lineageMap.getOrDefault(key, Collections.emptyList())
        );
    }

    /**
     * Returns all fields that have ever drifted for the given service.
     *
     * @param serviceName the microservice name
     * @return set of field names with recorded lineage
     */
    public Set<String> trackedFields(String serviceName) {
        Set<String> fields = new LinkedHashSet<>();
        String prefix = serviceName + ":";
        for (String key : lineageMap.keySet()) {
            if (key.startsWith(prefix)) {
                fields.add(key.substring(prefix.length()));
            }
        }
        return Collections.unmodifiableSet(fields);
    }

    /**
     * Clears all recorded lineage data.
     */
    public void clear() {
        lineageMap.clear();
    }

    private String buildKey(String serviceName, String fieldName) {
        return serviceName + ":" + fieldName;
    }
}
