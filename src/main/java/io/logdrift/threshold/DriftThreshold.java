package io.logdrift.threshold;

import java.util.Objects;

/**
 * Represents a configurable threshold for drift detection alerting.
 * Thresholds are defined per service and drift type.
 */
public class DriftThreshold {

    private final String serviceId;
    private final String driftType;
    private final int maxAllowedDrifts;
    private final long windowSeconds;
    private final boolean hardLimit;

    public DriftThreshold(String serviceId, String driftType, int maxAllowedDrifts,
                          long windowSeconds, boolean hardLimit) {
        if (serviceId == null || serviceId.isBlank()) throw new IllegalArgumentException("serviceId must not be blank");
        if (driftType == null || driftType.isBlank()) throw new IllegalArgumentException("driftType must not be blank");
        if (maxAllowedDrifts < 0) throw new IllegalArgumentException("maxAllowedDrifts must be >= 0");
        if (windowSeconds <= 0) throw new IllegalArgumentException("windowSeconds must be > 0");
        this.serviceId = serviceId;
        this.driftType = driftType;
        this.maxAllowedDrifts = maxAllowedDrifts;
        this.windowSeconds = windowSeconds;
        this.hardLimit = hardLimit;
    }

    public String getServiceId() { return serviceId; }
    public String getDriftType() { return driftType; }
    public int getMaxAllowedDrifts() { return maxAllowedDrifts; }
    public long getWindowSeconds() { return windowSeconds; }
    public boolean isHardLimit() { return hardLimit; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DriftThreshold)) return false;
        DriftThreshold that = (DriftThreshold) o;
        return maxAllowedDrifts == that.maxAllowedDrifts
                && windowSeconds == that.windowSeconds
                && hardLimit == that.hardLimit
                && Objects.equals(serviceId, that.serviceId)
                && Objects.equals(driftType, that.driftType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceId, driftType, maxAllowedDrifts, windowSeconds, hardLimit);
    }

    @Override
    public String toString() {
        return "DriftThreshold{serviceId='" + serviceId + "', driftType='" + driftType +
                "', maxAllowedDrifts=" + maxAllowedDrifts + ", windowSeconds=" + windowSeconds +
                ", hardLimit=" + hardLimit + '}';
    }
}
