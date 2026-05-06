package io.logdrift.classify;

import io.logdrift.drift.DriftEvent;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

/**
 * Classifies drift events into severity levels based on configurable rules.
 * Severity is determined by drift type, field criticality, and event volume.
 */
public class DriftSeverityClassifier {

    private final Map<DriftSeverity, Set<String>> criticalFieldsBySeverity;
    private final int highVolumeThreshold;

    public DriftSeverityClassifier(Map<DriftSeverity, Set<String>> criticalFieldsBySeverity,
                                   int highVolumeThreshold) {
        this.criticalFieldsBySeverity = new EnumMap<>(criticalFieldsBySeverity);
        this.highVolumeThreshold = highVolumeThreshold;
    }

    public DriftSeverityClassifier() {
        this(Map.of(
            DriftSeverity.CRITICAL, Set.of("userId", "traceId", "errorCode", "serviceId"),
            DriftSeverity.HIGH, Set.of("timestamp", "level", "message", "requestId")
        ), 100);
    }

    public DriftClassification classify(DriftEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("DriftEvent must not be null");
        }

        DriftSeverity severity = computeSeverity(event);
        String rationale = buildRationale(event, severity);
        return new DriftClassification(event, severity, rationale);
    }

    private DriftSeverity computeSeverity(DriftEvent event) {
        String field = event.getFieldName();

        Set<String> criticalFields = criticalFieldsBySeverity.getOrDefault(
                DriftSeverity.CRITICAL, Set.of());
        if (criticalFields.contains(field)) {
            return DriftSeverity.CRITICAL;
        }

        Set<String> highFields = criticalFieldsBySeverity.getOrDefault(
                DriftSeverity.HIGH, Set.of());
        if (highFields.contains(field)) {
            return DriftSeverity.HIGH;
        }

        if (event.getOccurrenceCount() >= highVolumeThreshold) {
            return DriftSeverity.HIGH;
        }

        if ("FIELD_REMOVED".equals(event.getDriftType())) {
            return DriftSeverity.MEDIUM;
        }

        return DriftSeverity.LOW;
    }

    private String buildRationale(DriftEvent event, DriftSeverity severity) {
        return String.format("Field '%s' classified as %s: driftType=%s, occurrences=%d",
                event.getFieldName(), severity, event.getDriftType(), event.getOccurrenceCount());
    }
}
