package io.logdrift.normalize;

import io.logdrift.drift.DriftEvent;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Normalizes DriftEvents to ensure consistent field naming, type labels,
 * and service identifiers before downstream processing or reporting.
 */
public class DriftEventNormalizer {

    private final NormalizationConfig config;

    public DriftEventNormalizer(NormalizationConfig config) {
        Objects.requireNonNull(config, "NormalizationConfig must not be null");
        this.config = config;
    }

    /**
     * Normalizes a single DriftEvent according to the active configuration.
     *
     * @param event the raw drift event
     * @return a new DriftEvent with normalized fields
     */
    public DriftEvent normalize(DriftEvent event) {
        Objects.requireNonNull(event, "DriftEvent must not be null");

        String service = normalizeServiceName(event.getServiceName());
        String field = normalizeFieldName(event.getFieldName());
        String driftType = normalizeDriftType(event.getDriftType());

        return new DriftEvent(service, field, driftType, event.getTimestamp(),
                event.getBaselineValue(), event.getObservedValue());
    }

    /**
     * Normalizes a batch of DriftEvents, filtering out null entries.
     *
     * @param events list of raw drift events
     * @return list of normalized drift events
     */
    public List<DriftEvent> normalizeAll(List<DriftEvent> events) {
        Objects.requireNonNull(events, "Events list must not be null");
        return events.stream()
                .filter(Objects::nonNull)
                .map(this::normalize)
                .collect(Collectors.toList());
    }

    private String normalizeServiceName(String serviceName) {
        if (serviceName == null || serviceName.isBlank()) {
            return config.getDefaultServiceName();
        }
        String normalized = serviceName.trim();
        return config.isLowercaseServiceNames() ? normalized.toLowerCase(Locale.ROOT) : normalized;
    }

    private String normalizeFieldName(String fieldName) {
        if (fieldName == null || fieldName.isBlank()) {
            return "unknown_field";
        }
        String normalized = fieldName.trim();
        if (config.isSnakeCaseFields()) {
            normalized = toSnakeCase(normalized);
        }
        return normalized;
    }

    private String normalizeDriftType(String driftType) {
        if (driftType == null || driftType.isBlank()) {
            return "UNKNOWN";
        }
        return driftType.trim().toUpperCase(Locale.ROOT);
    }

    private String toSnakeCase(String input) {
        return input.replaceAll("([a-z])([A-Z])", "$1_$2")
                    .replaceAll("[\\s\\-]+", "_")
                    .toLowerCase(Locale.ROOT);
    }
}
