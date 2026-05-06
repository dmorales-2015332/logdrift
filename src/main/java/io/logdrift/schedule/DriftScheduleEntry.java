package io.logdrift.schedule;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a single scheduled drift detection entry with execution state.
 */
public class DriftScheduleEntry {

    public enum Status { PENDING, RUNNING, COMPLETED, FAILED }

    private final DriftScheduleConfig config;
    private Instant lastRunAt;
    private Instant nextRunAt;
    private Status status;
    private int consecutiveFailures;

    public DriftScheduleEntry(DriftScheduleConfig config, Instant firstRunAt) {
        this.config = Objects.requireNonNull(config);
        this.nextRunAt = Objects.requireNonNull(firstRunAt);
        this.status = Status.PENDING;
        this.consecutiveFailures = 0;
    }

    public DriftScheduleConfig getConfig() { return config; }
    public Instant getLastRunAt() { return lastRunAt; }
    public Instant getNextRunAt() { return nextRunAt; }
    public Status getStatus() { return status; }
    public int getConsecutiveFailures() { return consecutiveFailures; }

    public void markRunning() {
        this.status = Status.RUNNING;
        this.lastRunAt = Instant.now();
    }

    public void markCompleted() {
        this.status = Status.COMPLETED;
        this.consecutiveFailures = 0;
        this.nextRunAt = lastRunAt.plus(config.getInterval());
    }

    public void markFailed() {
        this.status = Status.FAILED;
        this.consecutiveFailures++;
        this.nextRunAt = lastRunAt.plus(config.getInterval());
    }

    public boolean isDue(Instant now) {
        return !status.equals(Status.RUNNING) && !now.isBefore(nextRunAt);
    }

    public boolean hasExceededFailureLimit() {
        return consecutiveFailures >= config.getMaxConsecutiveFailures();
    }
}
