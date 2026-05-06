package io.logdrift.rollup;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Represents an aggregated summary of drift events within a single time bucket.
 */
public class DriftRollupEntry {

    private final Instant bucketStart;
    private final ChronoUnit bucketUnit;
    private final int eventCount;
    private final long totalAddedFields;
    private final long totalRemovedFields;
    private final long totalTypeChanges;

    public DriftRollupEntry(Instant bucketStart, ChronoUnit bucketUnit,
                             int eventCount, long totalAddedFields,
                             long totalRemovedFields, long totalTypeChanges) {
        this.bucketStart = Objects.requireNonNull(bucketStart, "bucketStart must not be null");
        this.bucketUnit = Objects.requireNonNull(bucketUnit, "bucketUnit must not be null");
        this.eventCount = eventCount;
        this.totalAddedFields = totalAddedFields;
        this.totalRemovedFields = totalRemovedFields;
        this.totalTypeChanges = totalTypeChanges;
    }

    public Instant getBucketStart() { return bucketStart; }
    public ChronoUnit getBucketUnit() { return bucketUnit; }
    public int getEventCount() { return eventCount; }
    public long getTotalAddedFields() { return totalAddedFields; }
    public long getTotalRemovedFields() { return totalRemovedFields; }
    public long getTotalTypeChanges() { return totalTypeChanges; }

    public long getTotalChanges() {
        return totalAddedFields + totalRemovedFields + totalTypeChanges;
    }

    @Override
    public String toString() {
        return String.format("DriftRollupEntry{bucket=%s, unit=%s, events=%d, added=%d, removed=%d, typeChanges=%d}",
                bucketStart, bucketUnit, eventCount, totalAddedFields, totalRemovedFields, totalTypeChanges);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DriftRollupEntry)) return false;
        DriftRollupEntry that = (DriftRollupEntry) o;
        return eventCount == that.eventCount
                && totalAddedFields == that.totalAddedFields
                && totalRemovedFields == that.totalRemovedFields
                && totalTypeChanges == that.totalTypeChanges
                && Objects.equals(bucketStart, that.bucketStart)
                && bucketUnit == that.bucketUnit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(bucketStart, bucketUnit, eventCount,
                totalAddedFields, totalRemovedFields, totalTypeChanges);
    }
}
