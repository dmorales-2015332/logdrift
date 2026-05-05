package io.logdrift.filter;

import io.logdrift.drift.DriftEvent;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

/**
 * Immutable criteria used to configure a {@link DriftEventFilter}.
 */
public class FilterCriteria {

    private final Set<DriftEvent.Severity> allowedSeverities;
    private final String serviceNamePattern;
    private final Set<String> excludedFields;

    private FilterCriteria(Builder builder) {
        this.allowedSeverities = Collections.unmodifiableSet(
                builder.allowedSeverities.isEmpty()
                        ? EnumSet.noneOf(DriftEvent.Severity.class)
                        : EnumSet.copyOf(builder.allowedSeverities));
        this.serviceNamePattern = builder.serviceNamePattern;
        this.excludedFields = Collections.unmodifiableSet(new HashSet<>(builder.excludedFields));
    }

    public Set<DriftEvent.Severity> getAllowedSeverities() {
        return allowedSeverities;
    }

    public String getServiceNamePattern() {
        return serviceNamePattern;
    }

    public Set<String> getExcludedFields() {
        return excludedFields;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final Set<DriftEvent.Severity> allowedSeverities = new HashSet<>();
        private String serviceNamePattern;
        private final Set<String> excludedFields = new HashSet<>();

        public Builder allowSeverity(DriftEvent.Severity severity) {
            this.allowedSeverities.add(severity);
            return this;
        }

        public Builder serviceNamePattern(String pattern) {
            this.serviceNamePattern = pattern;
            return this;
        }

        public Builder excludeField(String fieldName) {
            this.excludedFields.add(fieldName);
            return this;
        }

        public FilterCriteria build() {
            return new FilterCriteria(this);
        }
    }
}
