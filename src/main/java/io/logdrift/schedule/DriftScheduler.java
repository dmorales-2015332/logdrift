package io.logdrift.schedule;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Manages and triggers scheduled drift detection runs for registered services.
 */
public class DriftScheduler {

    private final Map<String, DriftScheduleEntry> entries = new ConcurrentHashMap<>();
    private final Consumer<DriftScheduleEntry> runHandler;
    private final Consumer<DriftScheduleEntry> failureLimitHandler;

    public DriftScheduler(Consumer<DriftScheduleEntry> runHandler,
                          Consumer<DriftScheduleEntry> failureLimitHandler) {
        this.runHandler = Objects.requireNonNull(runHandler);
        this.failureLimitHandler = Objects.requireNonNull(failureLimitHandler);
    }

    public void register(DriftScheduleConfig config) {
        register(config, Instant.now());
    }

    public void register(DriftScheduleConfig config, Instant firstRunAt) {
        entries.put(config.getServiceId(), new DriftScheduleEntry(config, firstRunAt));
    }

    public boolean unregister(String serviceId) {
        return entries.remove(serviceId) != null;
    }

    public Optional<DriftScheduleEntry> getEntry(String serviceId) {
        return Optional.ofNullable(entries.get(serviceId));
    }

    public List<DriftScheduleEntry> getAllEntries() {
        return Collections.unmodifiableList(new ArrayList<>(entries.values()));
    }

    /**
     * Tick: evaluates all entries and triggers due ones.
     * Should be called periodically by an external scheduler.
     */
    public void tick() {
        Instant now = Instant.now();
        for (DriftScheduleEntry entry : entries.values()) {
            if (entry.isDue(now)) {
                entry.markRunning();
                try {
                    runHandler.accept(entry);
                    entry.markCompleted();
                } catch (Exception e) {
                    entry.markFailed();
                    if (entry.hasExceededFailureLimit()) {
                        failureLimitHandler.accept(entry);
                    }
                }
            }
        }
    }

    public int size() {
        return entries.size();
    }
}
