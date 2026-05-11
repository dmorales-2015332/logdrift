package io.logdrift.checkpoint;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class DriftCheckpointServiceTest {

    private DriftCheckpointStore store;
    private DriftCheckpointService service;

    @BeforeEach
    void setUp() {
        store = new DriftCheckpointStore();
        service = new DriftCheckpointService(store);
    }

    @Test
    void createCheckpoint_shouldPersistAndReturnCheckpoint() {
        DriftCheckpoint cp = service.createCheckpoint("auth-service", "v1.0", "abc123", 5, null);
        assertNotNull(cp.getId());
        assertEquals("auth-service", cp.getServiceName());
        assertEquals("v1.0", cp.getLabel());
        assertEquals("abc123", cp.getSchemaHash());
        assertEquals(5, cp.getDriftEventCount());
    }

    @Test
    void getCheckpoint_shouldReturnStoredCheckpoint() {
        DriftCheckpoint cp = service.createCheckpoint("order-service", "release", "hash1", 2, "meta");
        Optional<DriftCheckpoint> found = service.getCheckpoint(cp.getId());
        assertTrue(found.isPresent());
        assertEquals(cp.getId(), found.get().getId());
    }

    @Test
    void getCheckpoint_unknownId_shouldReturnEmpty() {
        assertTrue(service.getCheckpoint("nonexistent").isEmpty());
    }

    @Test
    void getCheckpointsForService_shouldFilterByService() {
        service.createCheckpoint("svc-a", "cp1", "h1", 1, null);
        service.createCheckpoint("svc-a", "cp2", "h2", 3, null);
        service.createCheckpoint("svc-b", "cp1", "h3", 0, null);

        List<DriftCheckpoint> result = service.getCheckpointsForService("svc-a");
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(c -> c.getServiceName().equals("svc-a")));
    }

    @Test
    void diff_shouldDetectSchemaChange() {
        DriftCheckpoint cp1 = service.createCheckpoint("svc", "before", "hashA", 2, null);
        DriftCheckpoint cp2 = service.createCheckpoint("svc", "after", "hashB", 5, null);

        Optional<CheckpointDiff> diff = service.diff(cp1.getId(), cp2.getId());
        assertTrue(diff.isPresent());
        assertTrue(diff.get().isSchemaChanged());
        assertEquals(3, diff.get().getDriftEventDelta());
        assertTrue(diff.get().hasDrift());
    }

    @Test
    void diff_shouldReturnEmptyWhenCheckpointMissing() {
        DriftCheckpoint cp = service.createCheckpoint("svc", "label", "hash", 0, null);
        assertTrue(service.diff(cp.getId(), "missing").isEmpty());
    }

    @Test
    void diff_noChanges_hasDriftShouldBeFalse() {
        DriftCheckpoint cp1 = service.createCheckpoint("svc", "v1", "sameHash", 4, null);
        DriftCheckpoint cp2 = service.createCheckpoint("svc", "v2", "sameHash", 4, null);
        Optional<CheckpointDiff> diff = service.diff(cp1.getId(), cp2.getId());
        assertTrue(diff.isPresent());
        assertFalse(diff.get().hasDrift());
    }

    @Test
    void deleteCheckpoint_shouldRemoveFromStore() {
        DriftCheckpoint cp = service.createCheckpoint("svc", "lbl", "h", 0, null);
        assertTrue(service.deleteCheckpoint(cp.getId()));
        assertTrue(service.getCheckpoint(cp.getId()).isEmpty());
    }

    @Test
    void listAll_shouldReturnAllCheckpoints() {
        service.createCheckpoint("a", "l1", "h1", 0, null);
        service.createCheckpoint("b", "l2", "h2", 1, null);
        assertEquals(2, service.listAll().size());
    }
}
