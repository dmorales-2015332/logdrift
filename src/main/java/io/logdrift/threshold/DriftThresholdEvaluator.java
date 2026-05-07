package io.logdrift.threshold;

import io.logdrift.drift.DriftEvent;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Evaluates drift events against registered thresholds.
 * Tracks event counts per service/type within a rolling window.
 */
public class DriftThresholdEvaluator {

    private final DriftThresholdRegistry registry;
    // key -> list of event timestamps
    private final Map<String, List<Instant>> eventLog = new ConcurrentHashMap<>();

    public DriftThresholdEvaluator(DriftThresholdRegistry registry) {
        this.registry = Objects.requireNonNull(registry, "registry must not be null");
    }

    /**
     * Records a drift event and evaluates it against the applicable threshold.
     *
     * @return a ThresholdViolation if the threshold is breached, otherwise empty
     */
    public Optional<ThresholdViolation> evaluate(DriftEvent event) {
        Objects.requireNonNull(event, "event must not be null");
        String serviceId = event.getServiceId();
        String driftType = event.getDriftType();

        Optional<DriftThreshold> opt = registry.find(serviceId, driftType);
        if (opt.isEmpty()) return Optional.empty();

        DriftThreshold threshold = opt.get();
        String logKey = serviceId + ":" + driftType;
        Instant now = Instant.now();
        Instant windowStart = now.minusSeconds(threshold.getWindowSeconds());

        List<Instant> timestamps = eventLog.computeIfAbsent(logKey, k -> new ArrayList<>());
        synchronized (timestamps) {
            timestamps.add(now);
            // prune old entries outside the window
            timestamps.removeIf(t -> t.isBefore(windowStart));
            int count = timestamps.size();
            if (count > threshold.getMaxAllowedDrifts()) {
                return Optional.of(new ThresholdViolation(threshold, count, now));
            }
        }
        return Optional.empty();
    }

    public void reset(String serviceId, String driftType) {
        eventLog.remove(serviceId + ":" + driftType);
    }

    public void resetAll() {
        eventLog.clear();
    }
}
