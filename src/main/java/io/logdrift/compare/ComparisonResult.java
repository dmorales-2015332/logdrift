package io.logdrift.compare;

import io.logdrift.drift.DriftEvent;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Immutable value object that bundles the outcome of a schema comparison
 * together with metadata useful for reporting.
 */
public final class ComparisonResult {

    private final String service;
    private final String baselineVersion;
    private final String candidateVersion;
    private final List<DriftEvent> driftEvents;
    private final Instant comparedAt;

    public ComparisonResult(
            String service,
            String baselineVersion,
            String candidateVersion,
            List<DriftEvent> driftEvents) {
        if (service == null || service.isBlank()) {
            throw new IllegalArgumentException("service must not be null or blank");
        }
        if (driftEvents == null) {
            throw new IllegalArgumentException("driftEvents must not be null");
        }
        this.service          = service;
        this.baselineVersion  = baselineVersion;
        this.candidateVersion = candidateVersion;
        this.driftEvents      = Collections.unmodifiableList(driftEvents);
        this.comparedAt       = Instant.now();
    }

    public String getService()           { return service; }
    public String getBaselineVersion()   { return baselineVersion; }
    public String getCandidateVersion()  { return candidateVersion; }
    public List<DriftEvent> getDriftEvents() { return driftEvents; }
    public Instant getComparedAt()       { return comparedAt; }

    public boolean hasDrift() {
        return !driftEvents.isEmpty();
    }

    public int driftCount() {
        return driftEvents.size();
    }

    /**
     * Returns a filtered view of drift events matching the given severity level.
     *
     * @param severity the severity string to filter by (case-insensitive)
     * @return an unmodifiable list of matching {@link DriftEvent} instances
     */
    public List<DriftEvent> getDriftEventsBySeverity(String severity) {
        if (severity == null) {
            return Collections.emptyList();
        }
        return driftEvents.stream()
                .filter(e -> severity.equalsIgnoreCase(e.getSeverity()))
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public String toString() {
        return String.format(
            "ComparisonResult{service='%s', baseline='%s', candidate='%s', driftCount=%d, comparedAt=%s}",
            service, baselineVersion, candidateVersion, driftCount(), comparedAt);
    }
}
