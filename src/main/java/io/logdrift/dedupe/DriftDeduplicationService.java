package io.logdrift.dedupe;

import io.logdrift.drift.DriftEvent;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Deduplicates drift events within a configurable time window to prevent
 * alert fatigue from repeated identical schema drift detections.
 */
public class DriftDeduplicationService {

    private final Duration deduplicationWindow;
    private final Map<String, Instant> seenEventKeys;
    private final int maxCacheSize;

    public DriftDeduplicationService(Duration deduplicationWindow, int maxCacheSize) {
        if (deduplicationWindow == null || deduplicationWindow.isNegative() || deduplicationWindow.isZero()) {
            throw new IllegalArgumentException("Deduplication window must be a positive duration");
        }
        if (maxCacheSize <= 0) {
            throw new IllegalArgumentException("Max cache size must be positive");
        }
        this.deduplicationWindow = deduplicationWindow;
        this.maxCacheSize = maxCacheSize;
        this.seenEventKeys = new LinkedHashMap<>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Instant> eldest) {
                return size() > maxCacheSize;
            }
        };
    }

    /**
     * Filters a list of drift events, removing duplicates seen within the window.
     */
    public List<DriftEvent> deduplicate(List<DriftEvent> events) {
        Objects.requireNonNull(events, "Events list must not be null");
        Instant now = Instant.now();
        evictExpired(now);
        return events.stream()
                .filter(event -> isNew(event, now))
                .collect(Collectors.toList());
    }

    /**
     * Returns true if the event has not been seen within the deduplication window.
     */
    public boolean isNew(DriftEvent event, Instant referenceTime) {
        Objects.requireNonNull(event, "DriftEvent must not be null");
        String key = buildKey(event);
        Instant lastSeen = seenEventKeys.get(key);
        if (lastSeen == null || referenceTime.isAfter(lastSeen.plus(deduplicationWindow))) {
            seenEventKeys.put(key, referenceTime);
            return true;
        }
        return false;
    }

    public void clearCache() {
        seenEventKeys.clear();
    }

    public int cacheSize() {
        return seenEventKeys.size();
    }

    private void evictExpired(Instant now) {
        seenEventKeys.entrySet().removeIf(entry ->
                now.isAfter(entry.getValue().plus(deduplicationWindow)));
    }

    private String buildKey(DriftEvent event) {
        return event.getServiceName() + "|" + event.getFieldName() + "|" + event.getDriftType();
    }
}
