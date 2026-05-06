package io.logdrift.rollup;

import io.logdrift.drift.DriftEvent;
import io.logdrift.metrics.DriftMetricsSummary;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Aggregates drift events into time-bucketed rollup summaries
 * for trend analysis and reporting.
 */
public class DriftRollupService {

    private final ChronoUnit bucketUnit;

    public DriftRollupService(ChronoUnit bucketUnit) {
        if (bucketUnit == null) {
            throw new IllegalArgumentException("Bucket unit must not be null");
        }
        this.bucketUnit = bucketUnit;
    }

    /**
     * Rolls up a list of drift events into time-bucketed rollup entries.
     *
     * @param events the drift events to aggregate
     * @return list of rollup entries, one per time bucket
     */
    public List<DriftRollupEntry> rollup(List<DriftEvent> events) {
        if (events == null || events.isEmpty()) {
            return List.of();
        }

        Map<Instant, List<DriftEvent>> bucketed = events.stream()
                .collect(Collectors.groupingBy(e -> truncate(e.getTimestamp())));

        return bucketed.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> buildEntry(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    private DriftRollupEntry buildEntry(Instant bucket, List<DriftEvent> events) {
        long addedFields = events.stream()
                .mapToLong(e -> e.getAddedFields() != null ? e.getAddedFields().size() : 0)
                .sum();
        long removedFields = events.stream()
                .mapToLong(e -> e.getRemovedFields() != null ? e.getRemovedFields().size() : 0)
                .sum();
        long typeChanges = events.stream()
                .mapToLong(e -> e.getTypeChanges() != null ? e.getTypeChanges().size() : 0)
                .sum();

        return new DriftRollupEntry(bucket, bucketUnit, events.size(),
                addedFields, removedFields, typeChanges);
    }

    private Instant truncate(Instant instant) {
        return instant.truncatedTo(bucketUnit);
    }

    public ChronoUnit getBucketUnit() {
        return bucketUnit;
    }
}
