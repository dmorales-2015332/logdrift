package io.logdrift.threshold;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for managing drift thresholds keyed by serviceId + driftType.
 */
public class DriftThresholdRegistry {

    private final Map<String, DriftThreshold> thresholds = new ConcurrentHashMap<>();

    private static String key(String serviceId, String driftType) {
        return serviceId + ":" + driftType;
    }

    public void register(DriftThreshold threshold) {
        Objects.requireNonNull(threshold, "threshold must not be null");
        thresholds.put(key(threshold.getServiceId(), threshold.getDriftType()), threshold);
    }

    public Optional<DriftThreshold> find(String serviceId, String driftType) {
        return Optional.ofNullable(thresholds.get(key(serviceId, driftType)));
    }

    public boolean remove(String serviceId, String driftType) {
        return thresholds.remove(key(serviceId, driftType)) != null;
    }

    public List<DriftThreshold> listAll() {
        return Collections.unmodifiableList(new ArrayList<>(thresholds.values()));
    }

    public List<DriftThreshold> listByService(String serviceId) {
        List<DriftThreshold> result = new ArrayList<>();
        for (DriftThreshold t : thresholds.values()) {
            if (t.getServiceId().equals(serviceId)) result.add(t);
        }
        return Collections.unmodifiableList(result);
    }

    public void clear() {
        thresholds.clear();
    }

    public int size() {
        return thresholds.size();
    }
}
