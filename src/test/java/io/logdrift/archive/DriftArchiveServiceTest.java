package io.logdrift.archive;

import io.logdrift.drift.DriftEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class DriftArchiveServiceTest {

    private DriftArchiveService service;
    private DriftEvent mockEvent;

    @BeforeEach
    void setUp() {
        service = new DriftArchiveService(new DriftArchiveStore());
        mockEvent = mock(DriftEvent.class);
    }

    @Test
    void archive_createsEntryWithGeneratedId() {
        DriftArchiveEntry entry = service.archive(mockEvent, "svc-X", "RESOLVED");
        assertNotNull(entry.getArchiveId());
        assertEquals("svc-X", entry.getServiceId());
        assertEquals("RESOLVED", entry.getReason());
        assertEquals(1, service.archiveCount());
    }

    @Test
    void retrieve_returnsArchivedEntry() {
        DriftArchiveEntry archived = service.archive(mockEvent, "svc-X", "AUTO");
        Optional<DriftArchiveEntry> result = service.retrieve(archived.getArchiveId());
        assertTrue(result.isPresent());
        assertEquals(archived.getArchiveId(), result.get().getArchiveId());
    }

    @Test
    void retrieveForService_returnsAllForService() {
        service.archive(mockEvent, "svc-A", "R1");
        service.archive(mockEvent, "svc-A", "R2");
        service.archive(mockEvent, "svc-B", "R3");
        List<DriftArchiveEntry> results = service.retrieveForService("svc-A");
        assertEquals(2, results.size());
    }

    @Test
    void retrieveBetween_throwsWhenFromAfterTo() {
        Instant now = Instant.now();
        assertThrows(IllegalArgumentException.class,
                () -> service.retrieveBetween(now.plusSeconds(10), now));
    }

    @Test
    void purge_removesEntry() {
        DriftArchiveEntry entry = service.archive(mockEvent, "svc-X", "MANUAL");
        assertTrue(service.purge(entry.getArchiveId()));
        assertEquals(0, service.archiveCount());
        assertTrue(service.retrieve(entry.getArchiveId()).isEmpty());
    }

    @Test
    void purge_nonExistentId_returnsFalse() {
        assertFalse(service.purge("does-not-exist"));
    }

    @Test
    void archive_nullEvent_throwsNPE() {
        assertThrows(NullPointerException.class,
                () -> service.archive(null, "svc-X", "R"));
    }

    @Test
    void archive_nullServiceId_throwsNPE() {
        assertThrows(NullPointerException.class,
                () -> service.archive(mockEvent, null, "R"));
    }
}
