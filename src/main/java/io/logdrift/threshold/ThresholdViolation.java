package io.logdrift.threshold;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a threshold violation produced when drift event counts exceed
 * the configured limit within the observation window.
 */
public class ThresholdViolation {

    private final DriftThreshold threshold;
    private final int observedCount;
    private final Instant detectedAt;

    public ThresholdViolation(DriftThreshold threshold, int observedCount, Instant detectedAt) {
        this.threshold = Objects.requireNonNull(threshold, "threshold must not be null");
        if (observedCount < 0) throw new IllegalArgumentException("observedCount must be >= 0");
        this.observedCount = observedCount;
        this.detectedAt = Objects.requireNonNull(detectedAt, "detectedAt must not be null");
    }

    public DriftThreshold getThreshold() { return threshold; }
    public int getObservedCount() { return observedCount; }
    public Instant getDetectedAt() { return detectedAt; }

    public boolean isHardLimitViolation() {
        return threshold.isHardLimit();
    }

    public int getExcess() {
        return observedCount - threshold.getMaxAllowedDrifts();
    }

    @Override
    public String toString() {
        return "ThresholdViolation{service='" + threshold.getServiceId() +
                "', driftType='" + threshold.getDriftType() +
                "', observed=" + observedCount +
                ", limit=" + threshold.getMaxAllowedDrifts() +
                ", hard=" + threshold.isHardLimit() +
                ", detectedAt=" + detectedAt + '}';
    }
}
