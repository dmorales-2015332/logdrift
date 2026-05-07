package io.logdrift.window;

import io.logdrift.drift.DriftEvent;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a single time-bounded bucket that accumulates {@link DriftEvent}s
 * within a window interval.
 */
public class DriftWindowBucket {

    private final Instant windowStart;
    private final Instant windowEnd;
    private final List<DriftEvent> events = new ArrayList<>();

    public DriftWindowBucket(Instant windowStart, Instant windowEnd) {
        this.windowStart = Objects.requireNonNull(windowStart);
        this.windowEnd = Objects.requireNonNull(windowEnd);
    }

    public boolean accepts(Instant eventTime) {
        return !eventTime.isBefore(windowStart) && eventTime.isBefore(windowEnd);
    }

    public void add(DriftEvent event) {
        events.add(Objects.requireNonNull(event));
    }

    public List<DriftEvent> getEvents() {
        return Collections.unmodifiableList(events);
    }

    public int getEventCount() {
        return events.size();
    }

    public Instant getWindowStart() { return windowStart; }
    public Instant getWindowEnd() { return windowEnd; }

    public boolean isExpired(Instant now) {
        return now.isAfter(windowEnd);
    }

    @Override
    public String toString() {
        return "DriftWindowBucket{start=" + windowStart +
               ", end=" + windowEnd +
               ", events=" + events.size() + "}";
    }
}
