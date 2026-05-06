package io.logdrift.metrics;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;

/**
 * Immutable summary of drift metrics collected over a time window.
 */
public class DriftMetricsSummary {

    private final long totalDriftEvents;
    private final long totalFieldAdditions;
    private final long totalFieldRemovals;
    private final long totalTypeChanges;
    private final Map<String, Long> driftCountByService;
    private final Instant windowStart;
    private final Instant windowEnd;

    public DriftMetricsSummary(
            long totalDriftEvents,
            long totalFieldAdditions,
            long totalFieldRemovals,
            long totalTypeChanges,
            Map<String, Long> driftCountByService,
            Instant windowStart,
            Instant windowEnd) {
        this.totalDriftEvents = totalDriftEvents;
        this.totalFieldAdditions = totalFieldAdditions;
        this.totalFieldRemovals = totalFieldRemovals;
        this.totalTypeChanges = totalTypeChanges;
        this.driftCountByService = Collections.unmodifiableMap(driftCountByService);
        this.windowStart = windowStart;
        this.windowEnd = windowEnd;
    }

    public long getTotalDriftEvents() { return totalDriftEvents; }
    public long getTotalFieldAdditions() { return totalFieldAdditions; }
    public long getTotalFieldRemovals() { return totalFieldRemovals; }
    public long getTotalTypeChanges() { return totalTypeChanges; }
    public Map<String, Long> getDriftCountByService() { return driftCountByService; }
    public Instant getWindowStart() { return windowStart; }
    public Instant getWindowEnd() { return windowEnd; }

    public String getMostDriftedService() {
        return driftCountByService.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    @Override
    public String toString() {
        return String.format(
                "DriftMetricsSummary{total=%d, additions=%d, removals=%d, typeChanges=%d, services=%d, window=[%s, %s]}",
                totalDriftEvents, totalFieldAdditions, totalFieldRemovals, totalTypeChanges,
                driftCountByService.size(), windowStart, windowEnd
        );
    }
}
