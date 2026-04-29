package io.logdrift.report;

import io.logdrift.drift.DriftEvent;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

/**
 * Immutable report summarizing schema drift events detected during a comparison run.
 */
public class DriftReport {

    private final String baselineVersion;
    private final String comparedVersion;
    private final Instant generatedAt;
    private final List<DriftEvent> events;

    public DriftReport(String baselineVersion, String comparedVersion, List<DriftEvent> events) {
        this.baselineVersion = baselineVersion;
        this.comparedVersion = comparedVersion;
        this.generatedAt = Instant.now();
        this.events = Collections.unmodifiableList(events);
    }

    public String getBaselineVersion() {
        return baselineVersion;
    }

    public String getComparedVersion() {
        return comparedVersion;
    }

    public Instant getGeneratedAt() {
        return generatedAt;
    }

    public List<DriftEvent> getEvents() {
        return events;
    }

    public boolean hasDrift() {
        return !events.isEmpty();
    }

    public int getDriftCount() {
        return events.size();
    }

    @Override
    public String toString() {
        return String.format("DriftReport{baseline='%s', compared='%s', driftCount=%d, generatedAt=%s}",
                baselineVersion, comparedVersion, getDriftCount(), generatedAt);
    }
}
