package io.logdrift.suppress;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a suppression rule that silences drift events matching
 * a given service and field pattern for a defined time window.
 */
public class DriftSuppression {

    private final String id;
    private final String servicePattern;
    private final String fieldPattern;
    private final Instant expiresAt;
    private final String reason;
    private final String createdBy;

    public DriftSuppression(String id, String servicePattern, String fieldPattern,
                            Instant expiresAt, String reason, String createdBy) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.servicePattern = Objects.requireNonNull(servicePattern, "servicePattern must not be null");
        this.fieldPattern = Objects.requireNonNull(fieldPattern, "fieldPattern must not be null");
        this.expiresAt = Objects.requireNonNull(expiresAt, "expiresAt must not be null");
        this.reason = reason;
        this.createdBy = createdBy;
    }

    public String getId() { return id; }
    public String getServicePattern() { return servicePattern; }
    public String getFieldPattern() { return fieldPattern; }
    public Instant getExpiresAt() { return expiresAt; }
    public String getReason() { return reason; }
    public String getCreatedBy() { return createdBy; }

    public boolean isExpired(Instant now) {
        return now.isAfter(expiresAt);
    }

    public boolean matches(String service, String field) {
        return service.matches(servicePattern.replace("*", ".*"))
                && field.matches(fieldPattern.replace("*", ".*"));
    }

    @Override
    public String toString() {
        return "DriftSuppression{id='" + id + "', service='" + servicePattern +
                "', field='" + fieldPattern + "', expiresAt=" + expiresAt + "}";
    }
}
