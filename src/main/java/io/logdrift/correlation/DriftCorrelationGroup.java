package io.logdrift.correlation;

import io.logdrift.drift.DriftEvent;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a group of correlated drift events sharing the same key.
 */
public class DriftCorrelationGroup {

    private final DriftCorrelationKey key;
    private final List<DriftEvent> events;
    private Instant firstSeen;
    private Instant lastSeen;

    public DriftCorrelationGroup(DriftCorrelationKey key) {
        this.key = Objects.requireNonNull(key, "key must not be null");
        this.events = new ArrayList<>();
    }

    public void addEvent(DriftEvent event) {
        Objects.requireNonNull(event, "event must not be null");
        events.add(event);
        Instant ts = event.getDetectedAt();
        if (firstSeen == null || ts.isBefore(firstSeen)) firstSeen = ts;
        if (lastSeen == null || ts.isAfter(lastSeen)) lastSeen = ts;
    }

    public DriftCorrelationKey getKey() {
        return key;
    }

    public List<DriftEvent> getEvents() {
        return Collections.unmodifiableList(events);
    }

    public int getEventCount() {
        return events.size();
    }

    public Instant getFirstSeen() {
        return firstSeen;
    }

    public Instant getLastSeen() {
        return lastSeen;
    }

    public boolean isRecurring() {
        return events.size() > 1;
    }
}
