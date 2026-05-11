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

    public void clear() {
        store.clear();
    }

    public int size() {
        return store.size();
    }
}
