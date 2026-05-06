package io.logdrift.replay;

import io.logdrift.drift.DriftEvent;
import io.logdrift.snapshot.SchemaSnapshot;
import io.logdrift.snapshot.SchemaSnapshotService;
import io.logdrift.compare.SchemaComparator;
import io.logdrift.compare.ComparisonResult;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Replays historical schema snapshots to reconstruct drift events
 * over a given time window. Useful for auditing and retroactive analysis.
 */
public class DriftReplayService {

    private final SchemaSnapshotService snapshotService;
    private final SchemaComparator comparator;

    public DriftReplayService(SchemaSnapshotService snapshotService, SchemaComparator comparator) {
        this.snapshotService = Objects.requireNonNull(snapshotService, "snapshotService must not be null");
        this.comparator = Objects.requireNonNull(comparator, "comparator must not be null");
    }

    /**
     * Replays drift detection across snapshots for the given service
     * between {@code from} (inclusive) and {@code to} (exclusive).
     *
     * @param serviceId the microservice identifier
     * @param from      start of replay window
     * @param to        end of replay window
     * @return ordered list of reconstructed drift events
     * @throws IllegalArgumentException if {@code from} is not before {@code to}
     */
    public List<DriftReplayResult> replay(String serviceId, Instant from, Instant to) {
        Objects.requireNonNull(serviceId, "serviceId must not be null");
        Objects.requireNonNull(from, "from must not be null");
        Objects.requireNonNull(to, "to must not be null");
        if (!from.isBefore(to)) {
            throw new IllegalArgumentException("'from' must be before 'to'");
        }

        List<SchemaSnapshot> snapshots = snapshotService.getSnapshotsInRange(serviceId, from, to);
        List<DriftReplayResult> results = new ArrayList<>();

        for (int i = 1; i < snapshots.size(); i++) {
            SchemaSnapshot previous = snapshots.get(i - 1);
            SchemaSnapshot current  = snapshots.get(i);
            ComparisonResult comparison = comparator.compare(previous.getSchema(), current.getSchema());
            if (comparison.hasDrift()) {
                results.add(new DriftReplayResult(serviceId, previous, current, comparison));
            }
        }
        return results;
    }

    /**
     * Returns the total number of drift-inducing transitions in the window.
     */
    public int countDriftTransitions(String serviceId, Instant from, Instant to) {
        return replay(serviceId, from, to).size();
    }

    /**
     * Returns whether any drift occurred for the given service within the specified window.
     * This is a convenience method equivalent to {@code countDriftTransitions(...) > 0},
     * but avoids unnecessary result accumulation when only presence is needed.
     *
     * @param serviceId the microservice identifier
     * @param from      start of replay window
     * @param to        end of replay window
     * @return {@code true} if at least one drift transition was detected
     */
    public boolean hasDriftInWindow(String serviceId, Instant from, Instant to) {
        Objects.requireNonNull(serviceId, "serviceId must not be null");
        Objects.requireNonNull(from, "from must not be null");
        Objects.requireNonNull(to, "to must not be null");
        if (!from.isBefore(to)) {
            throw new IllegalArgumentException("'from' must be before 'to'");
        }

        List<SchemaSnapshot> snapshots = snapshotService.getSnapshotsInRange(serviceId, from, to);
        for (int i = 1; i < snapshots.size(); i++) {
            ComparisonResult comparison = comparator.compare(
                    snapshots.get(i - 1).getSchema(), snapshots.get(i).getSchema());
            if (comparison.hasDrift()) {
                return true;
            }
        }
        return false;
    }
}
