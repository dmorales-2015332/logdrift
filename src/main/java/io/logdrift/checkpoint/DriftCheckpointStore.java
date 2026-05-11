package io.logdrift.checkpoint;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory store for drift checkpoints, keyed by checkpoint ID.
 */
public class DriftCheckpointStore {

    private final Map<String, DriftCheckpoint> store = new ConcurrentHashMap<>();

    public void save(DriftCheckpoint checkpoint) {
        Objects.requireNonNull(checkpoint, "checkpoint must not be null");
        store.put(checkpoint.getId(), checkpoint);
    }

    public Optional<DriftCheckpoint> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    public List<DriftCheckpoint> findByService(String serviceName) {
        Objects.requireNonNull(serviceName, "serviceName must not be null");
        return store.values().stream()
                .filter(c -> c.getServiceName().equals(serviceName))
                .sorted(Comparator.comparing(DriftCheckpoint::getCreatedAt))
                .collect(Collectors.toList());
    }

    public List<DriftCheckpoint> findAll() {
        return store.values().stream()
                .sorted(Comparator.comparing(DriftCheckpoint::getCreatedAt))
                .collect(Collectors.toList());
    }

    public boolean delete(String id) {
        return store.remove(id) != null;
    }

    /**
     * Returns the most recent checkpoint for the given service, based on creation time.
     *
     * @param serviceName the name of the service to look up
     * @return an Optional containing the latest checkpoint, or empty if none exist
     */
    public Optional<DriftCheckpoint> findLatestByService(String serviceName) {
        Objects.requireNonNull(serviceName, "serviceName must not be null");
        return store.values().stream()
                .filter(c -> c.getServiceName().equals(serviceName))
                .max(Comparator.comparing(DriftCheckpoint::getCreatedAt));
    }

    public void clear() {
        store.clear();
    }

    public int size() {
        return store.size();
    }
}
