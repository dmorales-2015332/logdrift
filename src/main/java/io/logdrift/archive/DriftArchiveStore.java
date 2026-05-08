package io.logdrift.archive;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory store for archived drift events, keyed by archiveId.
 */
public class DriftArchiveStore {

    private final Map<String, DriftArchiveEntry> store = new ConcurrentHashMap<>();

    public void save(DriftArchiveEntry entry) {
        Objects.requireNonNull(entry, "entry must not be null");
        store.put(entry.getArchiveId(), entry);
    }

    public Optional<DriftArchiveEntry> findById(String archiveId) {
        return Optional.ofNullable(store.get(archiveId));
    }

    public List<DriftArchiveEntry> findByServiceId(String serviceId) {
        return store.values().stream()
                .filter(e -> e.getServiceId().equals(serviceId))
                .sorted(Comparator.comparing(DriftArchiveEntry::getArchivedAt))
                .collect(Collectors.toList());
    }

    public List<DriftArchiveEntry> findBetween(Instant from, Instant to) {
        return store.values().stream()
                .filter(e -> !e.getArchivedAt().isBefore(from) && !e.getArchivedAt().isAfter(to))
                .sorted(Comparator.comparing(DriftArchiveEntry::getArchivedAt))
                .collect(Collectors.toList());
    }

    public boolean delete(String archiveId) {
        return store.remove(archiveId) != null;
    }

    public int size() {
        return store.size();
    }

    public void clear() {
        store.clear();
    }
}
