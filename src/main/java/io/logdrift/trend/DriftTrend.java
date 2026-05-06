package io.logdrift.trend;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Represents a computed drift trend for a specific service over a time window.
 */
public class DriftTrend {

    private final String serviceId;
    private final Instant windowStart;
    private final Instant windowEnd;
    private final int totalDriftEvents;
    private final double driftRate; // events per hour
    private final TrendDirection direction;
    private final List<DriftTrendPoint> dataPoints;

    public DriftTrend(String serviceId, Instant windowStart, Instant windowEnd,
                      int totalDriftEvents, double driftRate,
                      TrendDirection direction, List<DriftTrendPoint> dataPoints) {
        this.serviceId = Objects.requireNonNull(serviceId, "serviceId must not be null");
        this.windowStart = Objects.requireNonNull(windowStart, "windowStart must not be null");
        this.windowEnd = Objects.requireNonNull(windowEnd, "windowEnd must not be null");
        this.totalDriftEvents = totalDriftEvents;
        this.driftRate = driftRate;
        this.direction = Objects.requireNonNull(direction, "direction must not be null");
        this.dataPoints = List.copyOf(Objects.requireNonNull(dataPoints));
    }

    public String getServiceId() { return serviceId; }
    public Instant getWindowStart() { return windowStart; }
    public Instant getWindowEnd() { return windowEnd; }
    public int getTotalDriftEvents() { return totalDriftEvents; }
    public double getDriftRate() { return driftRate; }
    public TrendDirection getDirection() { return direction; }
    public List<DriftTrendPoint> getDataPoints() { return dataPoints; }

    public enum TrendDirection {
        INCREASING, DECREASING, STABLE
    }

    @Override
    public String toString() {
        return String.format("DriftTrend{service='%s', direction=%s, rate=%.2f/hr, events=%d}",
                serviceId, direction, driftRate, totalDriftEvents);
    }
}
