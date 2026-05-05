package io.logdrift.compare;

import io.logdrift.drift.DriftEvent;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

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

    @Override
    public String toString() {
        return String.format(
            "ComparisonResult{service='%s', baseline='%s', candidate='%s', driftCount=%d, comparedAt=%s}",
            service, baselineVersion, candidateVersion, driftCount(), comparedAt);
    }
}
