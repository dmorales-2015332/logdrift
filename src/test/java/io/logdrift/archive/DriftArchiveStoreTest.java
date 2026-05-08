package io.logdrift.archive;

import io.logdrift.drift.DriftEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class DriftArchiveStoreTest {

    private DriftArchiveStore store;
    private DriftEvent mockEvent;

    @BeforeEach
    void setUp() {
        store = new DriftArchiveStore();
        mockEvent = mock(DriftEvent.class);
    }

    private DriftArchiveEntry entry(String id, String serviceId, Instant at) {
        return new DriftArchiveEntry(id, mockEvent, at, serviceId, "TEST");
    }

    @Test
    void saveAndFindById_returnsEntry() {
        DriftArchiveEntry e = entry("a1", "svc-A", Instant.now());
        store.save(e);
        Optional<DriftArchiveEntry> result = store.findById("a1");
        assertTrue(result.isPresent());
        assertEquals("a1", result.get().getArchiveId());
    }

    @Test
    void findById_missingKey_returnsEmpty() {
        assertTrue(store.findById("nonexistent").isEmpty());
    }

    @Test
    void findByServiceId_returnsMatchingEntries() {
        store.save(entry("a1", "svc-A", Instant.now()));
        store.save(entry("a2", "svc-B", Instant.now()));
        store.save(entry("a3", "svc-A", Instant.now()));
        List<DriftArchiveEntry> results = store.findByServiceId("svc-A");
        assertEquals(2, results.size());
        results.forEach(r -> assertEquals("svc-A", r.getServiceId()));
    }

    @Test
    void findBetween_returnsEntriesInRange() {
        Instant base = Instant.parse("2024-01-01T00:00:00Z");
        store.save(entry("a1", "svc-A", base.minusSeconds(10)));
        store.save(entry("a2", "svc-A", base));
        store.save(entry("a3", "svc-A", base.plusSeconds(10)));
        store.save(entry("a4", "svc-A", base.plusSeconds(100)));
        List<DriftArchiveEntry> results = store.findBetween(base, base.plusSeconds(10));
        assertEquals(2, results.size());
    }

    @Test
    void delete_removesEntry() {
        store.save(entry("a1", "svc-A", Instant.now()));
        assertTrue(store.delete("a1"));
        assertTrue(store.findById("a1").isEmpty());
    }

    @Test
    void delete_missingEntry_returnsFalse() {
        assertFalse(store.delete("ghost"));
    }

    @Test
    void size_reflectsCurrentCount() {
        assertEquals(0, store.size());
        store.save(entry("a1", "svc-A", Instant.now()));
        assertEquals(1, store.size());
        store.clear();
        assertEquals(0, store.size());
    }
}
