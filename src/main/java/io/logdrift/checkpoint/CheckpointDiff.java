package io.logdrift.checkpoint;

import java.time.Duration;
import java.util.Objects;

/**
 * Represents the difference between two drift checkpoints.
 */
public class CheckpointDiff {

    private final DriftCheckpoint from;
    private final DriftCheckpoint to;
    private final boolean schemaChanged;
    private final int driftEventDelta;

    public CheckpointDiff(DriftCheckpoint from, DriftCheckpoint to,
                          boolean schemaChanged, int driftEventDelta) {
        this.from = Objects.requireNonNull(from);
        this.to = Objects.requireNonNull(to);
        this.schemaChanged = schemaChanged;
        this.driftEventDelta = driftEventDelta;
    }

    public DriftCheckpoint getFrom() { return from; }
    public DriftCheckpoint getTo() { return to; }
    public boolean isSchemaChanged() { return schemaChanged; }
    public int getDriftEventDelta() { return driftEventDelta; }

    public Duration getTimeBetween() {
        return Duration.between(from.getCreatedAt(), to.getCreatedAt()).abs();
    }

    public boolean hasDrift() {
        return schemaChanged || driftEventDelta != 0;
    }

    @Override
    public String toString() {
        return "CheckpointDiff{from='" + from.getId() + "', to='" + to.getId() +
               "', schemaChanged=" + schemaChanged +
               ", driftDelta=" + driftEventDelta + "}";
    }
}
