package io.logdrift.audit;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Immutable audit log capturing a timestamped record of drift detection activity
 * for a specific service and deployment version.
 */
public class DriftAuditLog {

    private final String serviceId;
    private final String deploymentVersion;
    private final Instant detectedAt;
    private final List<String> driftSummaries;
    private final AuditSeverity severity;

    public DriftAuditLog(String serviceId,
                         String deploymentVersion,
                         Instant detectedAt,
                         List<String> driftSummaries,
                         AuditSeverity severity) {
        this.serviceId = Objects.requireNonNull(serviceId, "serviceId must not be null");
        this.deploymentVersion = Objects.requireNonNull(deploymentVersion, "deploymentVersion must not be null");
        this.detectedAt = Objects.requireNonNull(detectedAt, "detectedAt must not be null");
        this.driftSummaries = Collections.unmodifiableList(new ArrayList<>(Objects.requireNonNull(driftSummaries)));
        this.severity = Objects.requireNonNull(severity, "severity must not be null");
    }

    public String getServiceId() {
        return serviceId;
    }

    public String getDeploymentVersion() {
        return deploymentVersion;
    }

    public Instant getDetectedAt() {
        return detectedAt;
    }

    public List<String> getDriftSummaries() {
        return driftSummaries;
    }

    public AuditSeverity getSeverity() {
        return severity;
    }

    public boolean hasDrift() {
        return !driftSummaries.isEmpty();
    }

    @Override
    public String toString() {
        return "DriftAuditLog{" +
                "serviceId='" + serviceId + '\'' +
                ", deploymentVersion='" + deploymentVersion + '\'' +
                ", detectedAt=" + detectedAt +
                ", severity=" + severity +
                ", driftCount=" + driftSummaries.size() +
                '}';
    }

    public enum AuditSeverity {
        NONE, LOW, MEDIUM, HIGH, CRITICAL
    }
}
