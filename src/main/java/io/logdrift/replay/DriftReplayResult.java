package io.logdrift.replay;

import io.logdrift.compare.ComparisonResult;
import io.logdrift.snapshot.SchemaSnapshot;

import java.time.Instant;
import java.util.Objects;

/**
 * Captures the outcome of replaying a single schema transition
 * between two consecutive snapshots for a given service.
 */
public class DriftReplayResult {

    private final String serviceId;
    private final SchemaSnapshot previousSnapshot;
    private final SchemaSnapshot currentSnapshot;
    private final ComparisonResult comparisonResult;

    public DriftReplayResult(
            String serviceId,
            SchemaSnapshot previousSnapshot,
            SchemaSnapshot currentSnapshot,
            ComparisonResult comparisonResult) {
        this.serviceId         = Objects.requireNonNull(serviceId);
        this.previousSnapshot  = Objects.requireNonNull(previousSnapshot);
        this.currentSnapshot   = Objects.requireNonNull(currentSnapshot);
        this.comparisonResult  = Objects.requireNonNull(comparisonResult);
    }

    public String getServiceId() {
        return serviceId;
    }

    public SchemaSnapshot getPreviousSnapshot() {
        return previousSnapshot;
    }

    public SchemaSnapshot getCurrentSnapshot() {
        return currentSnapshot;
    }

    public ComparisonResult getComparisonResult() {
        return comparisonResult;
    }

    public Instant getTransitionTime() {
        return currentSnapshot.getCapturedAt();
    }

    @Override
    public String toString() {
        return "DriftReplayResult{" +
                "serviceId='" + serviceId + '\'' +
                ", transitionTime=" + getTransitionTime() +
                ", driftTypes=" + comparisonResult.getDriftTypes() +
                '}';
    }
}
