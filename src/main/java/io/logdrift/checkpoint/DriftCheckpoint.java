package io.logdrift.checkpoint;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a named checkpoint capturing the state of drift detection
 * at a specific point in time for a given service.
 */
public class DriftCheckpoint {

    private final String id;
    private final String serviceName;
    private final String label;
    private final Instant createdAt;
    private final String schemaHash;
    private final int driftEventCount;
    private final String metadata;

    public DriftCheckpoint(String id, String serviceName, String label,
                           Instant createdAt, String schemaHash,
                           int driftEventCount, String metadata) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.serviceName = Objects.requireNonNull(serviceName, "serviceName must not be null");
        this.label = Objects.requireNonNull(label, "label must not be null");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.schemaHash = Objects.requireNonNull(schemaHash, "schemaHash must not be null");
        this.driftEventCount = driftEventCount;
        this.metadata = metadata;
    }

    public String getId() { return id; }
    public String getServiceName() { return serviceName; }
    public String getLabel() { return label; }
    public Instant getCreatedAt() { return createdAt; }
    public String getSchemaHash() { return schemaHash; }
    public int getDriftEventCount() { return driftEventCount; }
    public String getMetadata() { return metadata; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DriftCheckpoint)) return false;
        DriftCheckpoint that = (DriftCheckpoint) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "DriftCheckpoint{id='" + id + "', service='" + serviceName +
               "', label='" + label + "', createdAt=" + createdAt + "}";
    }
}
