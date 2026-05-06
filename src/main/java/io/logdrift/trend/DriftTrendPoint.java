package io.logdrift.trend;

import java.time.Instant;
import java.util.Objects;

/**
 * A single data point in a drift trend time series.
 */
public class DriftTrendPoint {

    private final Instant timestamp;
    private final int eventCount;
    private final double rollingRate;

    public DriftTrendPoint(Instant timestamp, int eventCount, double rollingRate) {
        this.timestamp = Objects.requireNonNull(timestamp, "timestamp must not be null");
        if (eventCount < 0) {
            throw new IllegalArgumentException("eventCount must be non-negative");
        }
        this.eventCount = eventCount;
        this.rollingRate = rollingRate;
    }

    public Instant getTimestamp() { return timestamp; }
    public int getEventCount() { return eventCount; }
    public double getRollingRate() { return rollingRate; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DriftTrendPoint)) return false;
        DriftTrendPoint that = (DriftTrendPoint) o;
        return eventCount == that.eventCount
                && Double.compare(that.rollingRate, rollingRate) == 0
                && Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, eventCount, rollingRate);
    }

    @Override
    public String toString() {
        return String.format("DriftTrendPoint{ts=%s, count=%d, rate=%.2f}",
                timestamp, eventCount, rollingRate);
    }
}
