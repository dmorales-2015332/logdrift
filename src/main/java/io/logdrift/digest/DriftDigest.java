package io.logdrift.digest;

import java.time.Instant;
import java.util.Map;

/**
 * Immutable value object representing a summarized view of drift activity
 * for a given service over a specific time window.
 */
public class DriftDigest {

    private final String service;
    private final Instant windowStart;
    private final Instant windowEnd;
    private final long totalDrifts;
    private final long totalFieldsAdded;
    private final long totalFieldsRemoved;
    private final Map<String, Long> driftsByType;

    public DriftDigest(String service, Instant windowStart, Instant windowEnd,
                       long totalDrifts, long totalFieldsAdded, long totalFieldsRemoved,
                       Map<String, Long> driftsByType) {
        this.service = service;
        this.windowStart = windowStart;
        this.windowEnd = windowEnd;
        this.totalDrifts = totalDrifts;
        this.totalFieldsAdded = totalFieldsAdded;
        this.totalFieldsRemoved = totalFieldsRemoved;
        this.driftsByType = Map.copyOf(driftsByType);
    }

    public String getService() { return service; }
    public Instant getWindowStart() { return windowStart; }
    public Instant getWindowEnd() { return windowEnd; }
    public long getTotalDrifts() { return totalDrifts; }
    public long getTotalFieldsAdded() { return totalFieldsAdded; }
    public long getTotalFieldsRemoved() { return totalFieldsRemoved; }
    public Map<String, Long> getDriftsByType() { return driftsByType; }

    public boolean isEmpty() {
        return totalDrifts == 0;
    }

    @Override
    public String toString() {
        return String.format("DriftDigest{service='%s', window=[%s, %s], totalDrifts=%d, added=%d, removed=%d}",
                service, windowStart, windowEnd, totalDrifts, totalFieldsAdded, totalFieldsRemoved);
    }
}
