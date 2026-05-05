package io.logdrift.filter;

import io.logdrift.drift.DriftEvent;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Filters DriftEvents based on configurable criteria such as severity,
 * field name patterns, or service name.
 */
public class DriftEventFilter {

    private final FilterCriteria criteria;

    public DriftEventFilter(FilterCriteria criteria) {
        if (criteria == null) {
            throw new IllegalArgumentException("FilterCriteria must not be null");
        }
        this.criteria = criteria;
    }

    public List<DriftEvent> apply(List<DriftEvent> events) {
        if (events == null || events.isEmpty()) {
            return List.of();
        }
        return events.stream()
                .filter(buildPredicate())
                .collect(Collectors.toList());
    }

    private Predicate<DriftEvent> buildPredicate() {
        Predicate<DriftEvent> predicate = e -> true;

        Set<DriftEvent.Severity> allowedSeverities = criteria.getAllowedSeverities();
        if (allowedSeverities != null && !allowedSeverities.isEmpty()) {
            predicate = predicate.and(e -> allowedSeverities.contains(e.getSeverity()));
        }

        String serviceNamePattern = criteria.getServiceNamePattern();
        if (serviceNamePattern != null && !serviceNamePattern.isBlank()) {
            predicate = predicate.and(e -> e.getServiceName() != null
                    && e.getServiceName().matches(serviceNamePattern));
        }

        Set<String> excludedFields = criteria.getExcludedFields();
        if (excludedFields != null && !excludedFields.isEmpty()) {
            predicate = predicate.and(e -> e.getFieldName() == null
                    || !excludedFields.contains(e.getFieldName()));
        }

        return predicate;
    }
}
