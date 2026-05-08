package io.logdrift.archive;

import io.logdrift.drift.DriftEvent;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Service for archiving resolved or acknowledged drift events.
 * Provides lifecycle management: archive, retrieve, and purge.
 */
public class DriftArchiveService {

    private static final Logger log = Logger.getLogger(DriftArchiveService.class.getName());

    private final DriftArchiveStore store;

    public DriftArchiveService(DriftArchiveStore store) {
        this.store = Objects.requireNonNull(store, "store must not be null");
    }

    public DriftArchiveEntry archive(DriftEvent event, String serviceId, String reason) {
        Objects.requireNonNull(event, "event must not be null");
        Objects.requireNonNull(serviceId, "serviceId must not be null");

        String archiveId = UUID.randomUUID().toString();
        DriftArchiveEntry entry = new DriftArchiveEntry(archiveId, event, Instant.now(), serviceId, reason);
        store.save(entry);
        log.info("Archived drift event: " + archiveId + " for service: " + serviceId);
        return entry;
    }

    public Optional<DriftArchiveEntry> retrieve(String archiveId) {
        return store.findById(archiveId);
    }

    public List<DriftArchiveEntry> retrieveForService(String serviceId) {
        return store.findByServiceId(serviceId);
    }

    public List<DriftArchiveEntry> retrieveBetween(Instant from, Instant to) {
        Objects.requireNonNull(from, "from must not be null");
        Objects.requireNonNull(to, "to must not be null");
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("'from' must not be after 'to'");
        }
        return store.findBetween(from, to);
    }

    public boolean purge(String archiveId) {
        boolean removed = store.delete(archiveId);
        if (removed) {
            log.info("Purged archive entry: " + archiveId);
        }
        return removed;
    }

    public int archiveCount() {
        return store.size();
    }
}
