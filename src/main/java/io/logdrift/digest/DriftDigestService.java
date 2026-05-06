package io.logdrift.digest;

import io.logdrift.drift.DriftEvent;
import io.logdrift.metrics.DriftMetricsSummary;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Produces a periodic digest summarizing drift activity across services.
 * Intended for use in scheduled reporting and notification pipelines.
 */
public class DriftDigestService {

    public DriftDigest buildDigest(String service, List<DriftEvent> events, Instant from, Instant to) {
        if (events == null || events.isEmpty()) {
            return new DriftDigest(service, from, to, 0, 0, 0, Map.of());
        }

        List<DriftEvent> inWindow = events.stream()
                .filter(e -> !e.getDetectedAt().isBefore(from) && !e.getDetectedAt().isAfter(to))
                .collect(Collectors.toList());

        long totalDrifts = inWindow.size();

        long addedFields = inWindow.stream()
                .flatMap(e -> e.getAddedFields().stream())
                .count();

        long removedFields = inWindow.stream()
                .flatMap(e -> e.getRemovedFields().stream())
                .count();

        Map<String, Long> driftsByType = inWindow.stream()
                .collect(Collectors.groupingBy(DriftEvent::getDriftType, Collectors.counting()));

        return new DriftDigest(service, from, to, totalDrifts, addedFields, removedFields, driftsByType);
    }

    public DriftDigest buildDigestFromSummary(String service, DriftMetricsSummary summary, Instant from, Instant to) {
        if (summary == null) {
            return new DriftDigest(service, from, to, 0, 0, 0, Map.of());
        }
        return new DriftDigest(
                service, from, to,
                summary.getTotalDriftsDetected(),
                summary.getTotalFieldsAdded(),
                summary.getTotalFieldsRemoved(),
                Map.of()
        );
    }
}
