package io.logdrift.archive;

import io.logdrift.drift.DriftEvent;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a single archived drift event with metadata.
 */
public class DriftArchiveEntry {

    private final String archiveId;
    private final DriftEvent event;
    private final Instant archivedAt;
    private final String serviceId;
    private final String reason;

    public DriftArchiveEntry(String archiveId, DriftEvent event, Instant archivedAt,
                             String serviceId, String reason) {
        this.archiveId = Objects.requireNonNull(archiveId, "archiveId must not be null");
        this.event = Objects.requireNonNull(event, "event must not be null");
        this.archivedAt = Objects.requireNonNull(archivedAt, "archivedAt must not be null");
        this.serviceId = Objects.requireNonNull(serviceId, "serviceId must not be null");
        this.reason = reason != null ? reason : "MANUAL";
    }

    public String getArchiveId() { return archiveId; }
    public DriftEvent getEvent() { return event; }
    public Instant getArchivedAt() { return archivedAt; }
    public String getServiceId() { return serviceId; }
    public String getReason() { return reason; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DriftArchiveEntry)) return false;
        DriftArchiveEntry that = (DriftArchiveEntry) o;
        return Objects.equals(archiveId, that.archiveId);
    }

    @Override
    public int hashCode() { return Objects.hash(archiveId); }

    @Override
    public String toString() {
        return "DriftArchiveEntry{archiveId='" + archiveId + "', serviceId='" + serviceId +
               "', archivedAt=" + archivedAt + ", reason='" + reason + "'}";
    }
}
