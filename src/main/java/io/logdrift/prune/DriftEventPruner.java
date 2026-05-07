package io.logdrift.prune;

import io.logdrift.drift.DriftEvent;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Prunes drift events from a collection based on configurable retention criteria
 * such as age, severity, and service name.
 */
public class DriftEventPruner {

    private final PrunePolicy policy;

    public DriftEventPruner(PrunePolicy policy) {
        this.policy = Objects.requireNonNull(policy, "PrunePolicy must not be null");
    }

    /**
     * Returns a new list containing only events that survive pruning.
     */
    public List<DriftEvent> prune(List<DriftEvent> events) {
        if (events == null || events.isEmpty()) {
            return List.of();
        }
        Predicate<DriftEvent> retainPredicate = buildRetainPredicate();
        List<DriftEvent> retained = new ArrayList<>();
        for (DriftEvent event : events) {
            if (retainPredicate.test(event)) {
                retained.add(event);
            }
        }
        return retained;
    }

    /**
     * Returns the count of events that would be pruned without modifying the list.
     */
    public int countPrunable(List<DriftEvent> events) {
        if (events == null || events.isEmpty()) {
            return 0;
        }
        Predicate<DriftEvent> retainPredicate = buildRetainPredicate();
        int prunable = 0;
        for (DriftEvent event : events) {
            if (!retainPredicate.test(event)) {
                prunable++;
            }
        }
        return prunable;
    }

    private Predicate<DriftEvent> buildRetainPredicate() {
        Predicate<DriftEvent> predicate = e -> true;

        if (policy.getMaxAgeSeconds() > 0) {
            Instant cutoff = Instant.now().minusSeconds(policy.getMaxAgeSeconds());
            predicate = predicate.and(e -> e.getDetectedAt() != null && e.getDetectedAt().isAfter(cutoff));
        }

        if (policy.getExcludedServices() != null && !policy.getExcludedServices().isEmpty()) {
            predicate = predicate.and(e -> !policy.getExcludedServices().contains(e.getServiceName()));
        }

        if (policy.getMinSeverity() != null) {
            predicate = predicate.and(e -> e.getSeverity() != null
                    && e.getSeverity().ordinal() >= policy.getMinSeverity().ordinal());
        }

        return predicate;
    }
}
