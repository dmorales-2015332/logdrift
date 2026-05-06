package io.logdrift.suppress;

import io.logdrift.drift.DriftEvent;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Wraps drift event processing with suppression awareness,
 * filtering out events that match active suppression rules.
 */
public class SuppressionAwareDriftFilter {

    private final DriftSuppressionRegistry registry;

    public SuppressionAwareDriftFilter(DriftSuppressionRegistry registry) {
        this.registry = Objects.requireNonNull(registry, "registry must not be null");
    }

    /**
     * Returns only events that are NOT currently suppressed.
     */
    public List<DriftEvent> filterSuppressed(List<DriftEvent> events) {
        Instant now = Instant.now();
        return events.stream()
                .filter(e -> !isSuppressed(e, now))
                .collect(Collectors.toList());
    }

    public boolean isSuppressed(DriftEvent event) {
        return isSuppressed(event, Instant.now());
    }

    private boolean isSuppressed(DriftEvent event, Instant now) {
        return registry.isSuppressed(
                event.getServiceName(),
                event.getFieldName(),
                now);
    }

    public SuppressionSummary summarize(List<DriftEvent> events) {
        Instant now = Instant.now();
        long suppressed = events.stream().filter(e -> isSuppressed(e, now)).count();
        long passed = events.size() - suppressed;
        return new SuppressionSummary((int) passed, (int) suppressed);
    }

    public record SuppressionSummary(int passed, int suppressed) {
        public int total() { return passed + suppressed; }
    }
}
