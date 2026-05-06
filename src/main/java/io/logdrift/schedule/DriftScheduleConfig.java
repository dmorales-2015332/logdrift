package io.logdrift.schedule;

import java.time.Duration;
import java.util.Objects;

/**
 * Configuration for scheduled drift detection runs.
 */
public class DriftScheduleConfig {

    private final String serviceId;
    private final Duration interval;
    private final boolean alertOnDrift;
    private final int maxConsecutiveFailures;

    private DriftScheduleConfig(Builder builder) {
        this.serviceId = Objects.requireNonNull(builder.serviceId, "serviceId must not be null");
        this.interval = Objects.requireNonNull(builder.interval, "interval must not be null");
        this.alertOnDrift = builder.alertOnDrift;
        this.maxConsecutiveFailures = builder.maxConsecutiveFailures;
    }

    public String getServiceId() { return serviceId; }
    public Duration getInterval() { return interval; }
    public boolean isAlertOnDrift() { return alertOnDrift; }
    public int getMaxConsecutiveFailures() { return maxConsecutiveFailures; }

    public static Builder builder(String serviceId) {
        return new Builder(serviceId);
    }

    public static class Builder {
        private final String serviceId;
        private Duration interval = Duration.ofMinutes(5);
        private boolean alertOnDrift = true;
        private int maxConsecutiveFailures = 3;

        private Builder(String serviceId) {
            this.serviceId = serviceId;
        }

        public Builder interval(Duration interval) { this.interval = interval; return this; }
        public Builder alertOnDrift(boolean alertOnDrift) { this.alertOnDrift = alertOnDrift; return this; }
        public Builder maxConsecutiveFailures(int max) { this.maxConsecutiveFailures = max; return this; }

        public DriftScheduleConfig build() { return new DriftScheduleConfig(this); }
    }

    @Override
    public String toString() {
        return "DriftScheduleConfig{serviceId='" + serviceId + "', interval=" + interval +
               ", alertOnDrift=" + alertOnDrift + ", maxConsecutiveFailures=" + maxConsecutiveFailures + "}";
    }
}
