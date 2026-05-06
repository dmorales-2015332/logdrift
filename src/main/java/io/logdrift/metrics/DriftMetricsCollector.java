package io.logdrift.metrics;

import io.logdrift.drift.DriftEvent;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Collects and aggregates drift metrics across services and time windows.
 */
public class DriftMetricsCollector {

    private final Map<String, AtomicLong> driftCountByService = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> fieldAdditionsByService = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> fieldRemovalsByService = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> typeChangesByService = new ConcurrentHashMap<>();
    private final Instant collectionStartTime;

    public DriftMetricsCollector() {
        this.collectionStartTime = Instant.now();
    }

    public void record(DriftEvent event) {
        if (event == null || event.getServiceName() == null) {
            return;
        }
        String service = event.getServiceName();
        driftCountByService.computeIfAbsent(service, k -> new AtomicLong()).incrementAndGet();

        switch (event.getDriftType()) {
            case FIELD_ADDED:
                fieldAdditionsByService.computeIfAbsent(service, k -> new AtomicLong()).incrementAndGet();
                break;
            case FIELD_REMOVED:
                fieldRemovalsByService.computeIfAbsent(service, k -> new AtomicLong()).incrementAndGet();
                break;
            case TYPE_CHANGED:
                typeChangesByService.computeIfAbsent(service, k -> new AtomicLong()).incrementAndGet();
                break;
            default:
                break;
        }
    }

    public void recordAll(List<DriftEvent> events) {
        if (events != null) {
            events.forEach(this::record);
        }
    }

    public DriftMetricsSummary getSummary() {
        long totalDrifts = driftCountByService.values().stream().mapToLong(AtomicLong::get).sum();
        long totalAdditions = fieldAdditionsByService.values().stream().mapToLong(AtomicLong::get).sum();
        long totalRemovals = fieldRemovalsByService.values().stream().mapToLong(AtomicLong::get).sum();
        long totalTypeChanges = typeChangesByService.values().stream().mapToLong(AtomicLong::get).sum();

        Map<String, Long> perServiceCounts = driftCountByService.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get()));

        return new DriftMetricsSummary(
                totalDrifts, totalAdditions, totalRemovals, totalTypeChanges,
                perServiceCounts, collectionStartTime, Instant.now()
        );
    }

    public void reset() {
        driftCountByService.clear();
        fieldAdditionsByService.clear();
        fieldRemovalsByService.clear();
        typeChangesByService.clear();
    }

    public long getDriftCountForService(String serviceName) {
        AtomicLong count = driftCountByService.get(serviceName);
        return count != null ? count.get() : 0L;
    }
}
