package io.logdrift.checkpoint;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Service for creating, retrieving, and comparing drift checkpoints.
 */
public class DriftCheckpointService {

    private static final Logger log = Logger.getLogger(DriftCheckpointService.class.getName());

    private final DriftCheckpointStore store;

    public DriftCheckpointService(DriftCheckpointStore store) {
        this.store = store;
    }

    public DriftCheckpoint createCheckpoint(String serviceName, String label,
                                             String schemaHash, int driftEventCount,
                                             String metadata) {
        String id = UUID.randomUUID().toString();
        DriftCheckpoint checkpoint = new DriftCheckpoint(
                id, serviceName, label, Instant.now(),
                schemaHash, driftEventCount, metadata);
        store.save(checkpoint);
        log.info("Created checkpoint: " + checkpoint);
        return checkpoint;
    }

    public Optional<DriftCheckpoint> getCheckpoint(String id) {
        return store.findById(id);
    }

    public List<DriftCheckpoint> getCheckpointsForService(String serviceName) {
        return store.findByService(serviceName);
    }

    public Optional<CheckpointDiff> diff(String fromId, String toId) {
        Optional<DriftCheckpoint> from = store.findById(fromId);
        Optional<DriftCheckpoint> to = store.findById(toId);
        if (from.isEmpty() || to.isEmpty()) {
            return Optional.empty();
        }
        DriftCheckpoint a = from.get();
        DriftCheckpoint b = to.get();
        boolean schemaChanged = !a.getSchemaHash().equals(b.getSchemaHash());
        int driftDelta = b.getDriftEventCount() - a.getDriftEventCount();
        return Optional.of(new CheckpointDiff(a, b, schemaChanged, driftDelta));
    }

    public boolean deleteCheckpoint(String id) {
        return store.delete(id);
    }

    public List<DriftCheckpoint> listAll() {
        return store.findAll();
    }
}
