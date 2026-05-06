package io.logdrift.classify;

import io.logdrift.drift.DriftEvent;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents the result of classifying a single DriftEvent with a severity level.
 */
public class DriftClassification {

    private final DriftEvent event;
    private final DriftSeverity severity;
    private final String rationale;
    private final Instant classifiedAt;

    public DriftClassification(DriftEvent event, DriftSeverity severity, String rationale) {
        this.event = Objects.requireNonNull(event, "event must not be null");
        this.severity = Objects.requireNonNull(severity, "severity must not be null");
        this.rationale = Objects.requireNonNull(rationale, "rationale must not be null");
        this.classifiedAt = Instant.now();
    }

    public DriftEvent getEvent() {
        return event;
    }

    public DriftSeverity getSeverity() {
        return severity;
    }

    public String getRationale() {
        return rationale;
    }

    public Instant getClassifiedAt() {
        return classifiedAt;
    }

    public boolean isAtLeast(DriftSeverity threshold) {
        return severity.ordinal() >= threshold.ordinal();
    }

    @Override
    public String toString() {
        return String.format("DriftClassification{field='%s', severity=%s, rationale='%s'}",
                event.getFieldName(), severity, rationale);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DriftClassification)) return false;
        DriftClassification that = (DriftClassification) o;
        return Objects.equals(event, that.event) && severity == that.severity;
    }

    @Override
    public int hashCode() {
        return Objects.hash(event, severity);
    }
}
