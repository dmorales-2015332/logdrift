package io.logdrift.snapshot;

import io.logdrift.schema.LogSchema;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a point-in-time capture of a LogSchema for a given service and version.
 */
public class SchemaSnapshot {

    private final String serviceId;
    private final String version;
    private final LogSchema schema;
    private final Instant capturedAt;

    public SchemaSnapshot(String serviceId, String version, LogSchema schema, Instant capturedAt) {
        this.serviceId = Objects.requireNonNull(serviceId, "serviceId must not be null");
        this.version = Objects.requireNonNull(version, "version must not be null");
        this.schema = Objects.requireNonNull(schema, "schema must not be null");
        this.capturedAt = Objects.requireNonNull(capturedAt, "capturedAt must not be null");
    }

    public String getServiceId() {
        return serviceId;
    }

    public String getVersion() {
        return version;
    }

    public LogSchema getSchema() {
        return schema;
    }

    public Instant getCapturedAt() {
        return capturedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SchemaSnapshot)) return false;
        SchemaSnapshot that = (SchemaSnapshot) o;
        return Objects.equals(serviceId, that.serviceId)
                && Objects.equals(version, that.version)
                && Objects.equals(capturedAt, that.capturedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceId, version, capturedAt);
    }

    @Override
    public String toString() {
        return "SchemaSnapshot{serviceId='" + serviceId + "', version='" + version
                + "', capturedAt=" + capturedAt + "}";
    }
}
