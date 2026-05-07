package io.logdrift.window;

import io.logdrift.drift.DriftEvent;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Aggregates {@link DriftEvent}s into time windows defined by a
 * {@link DriftWindowConfig}. Supports both tumbling and sliding windows.
 */
public class DriftWindowAggregator {

    private final DriftWindowConfig config;
    private final List<DriftWindowBucket> buckets = new ArrayList<>();

    public DriftWindowAggregator(DriftWindowConfig config) {
        this.config = config;
    }

    /**
     * Records a drift event, placing it into all applicable buckets.
     * Creates a new bucket if the event falls outside existing ones.
     */
    public void record(DriftEvent event, Instant eventTime) {
        evict(eventTime);
        boolean placed = false;
        for (DriftWindowBucket bucket : buckets) {
            if (bucket.accepts(eventTime)) {
                bucket.add(event);
                placed = true;
            }
        }
        if (!placed) {
            DriftWindowBucket newBucket = openBucket(eventTime);
            newBucket.add(event);
            buckets.add(newBucket);
        }
    }

    /**
     * Returns all non-expired buckets that are complete (window end has passed).
     */
    public List<DriftWindowBucket> drainCompleted(Instant now) {
        List<DriftWindowBucket> completed = buckets.stream()
                .filter(b -> b.isExpired(now))
                .collect(Collectors.toList());
        buckets.removeAll(completed);
        return completed;
    }

    public List<DriftWindowBucket> getActiveBuckets() {
        return List.copyOf(buckets);
    }

    public DriftWindowConfig getConfig() {
        return config;
    }

    private DriftWindowBucket openBucket(Instant anchorTime) {
        Instant start = anchorTime;
        Instant end = start.plus(config.getWindowSize());
        return new DriftWindowBucket(start, end);
    }

    private void evict(Instant now) {
        Instant cutoff = now.minus(config.getWindowSize());
        buckets.removeIf(b -> b.getWindowEnd().isBefore(cutoff));
    }
}
