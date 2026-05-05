package io.logdrift.snapshot;

import io.logdrift.schema.LogSchema;
import io.logdrift.schema.LogSchemaExtractor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchemaSnapshotServiceTest {

    @Mock
    private LogSchemaExtractor extractor;

    private SchemaSnapshotService service;

    private static final LogSchema SAMPLE_SCHEMA =
            new LogSchema(Map.of("level", "STRING", "ts", "TIMESTAMP"), Set.of("level", "ts"));

    @BeforeEach
    void setUp() {
        service = new SchemaSnapshotService(extractor);
    }

    @Test
    void capture_shouldReturnSnapshotWithCorrectServiceAndVersion() {
        when(extractor.extract(anyList())).thenReturn(SAMPLE_SCHEMA);

        SchemaSnapshot snapshot = service.capture("svc-payments", "v2.1", List.of("{}"));

        assertEquals("svc-payments", snapshot.getServiceId());
        assertEquals("v2.1", snapshot.getVersion());
        assertNotNull(snapshot.getCapturedAt());
    }

    @Test
    void getSnapshots_shouldReturnAllCapturedSnapshots() {
        when(extractor.extract(anyList())).thenReturn(SAMPLE_SCHEMA);

        service.capture("svc-a", "v1", List.of("{}"));
        service.capture("svc-a", "v2", List.of("{}"));

        List<SchemaSnapshot> snapshots = service.getSnapshots("svc-a");
        assertEquals(2, snapshots.size());
    }

    @Test
    void getLatest_shouldReturnMostRecentSnapshot() throws InterruptedException {
        when(extractor.extract(anyList())).thenReturn(SAMPLE_SCHEMA);

        service.capture("svc-b", "v1", List.of("{}"));
        Thread.sleep(5);
        service.capture("svc-b", "v2", List.of("{}"));

        SchemaSnapshot latest = service.getLatest("svc-b");
        assertNotNull(latest);
        assertEquals("v2", latest.getVersion());
    }

    @Test
    void getLatest_shouldReturnNullWhenNoSnapshotsExist() {
        assertNull(service.getLatest("unknown-service"));
    }

    @Test
    void clearSnapshots_shouldRemoveAllForService() {
        when(extractor.extract(anyList())).thenReturn(SAMPLE_SCHEMA);
        service.capture("svc-c", "v1", List.of("{}"));

        service.clearSnapshots("svc-c");

        assertTrue(service.getSnapshots("svc-c").isEmpty());
    }

    @Test
    void totalSnapshotCount_shouldAggregateAcrossServices() {
        when(extractor.extract(anyList())).thenReturn(SAMPLE_SCHEMA);
        service.capture("svc-x", "v1", List.of("{}"));
        service.capture("svc-x", "v2", List.of("{}"));
        service.capture("svc-y", "v1", List.of("{}"));

        assertEquals(3, service.totalSnapshotCount());
    }

    @Test
    void constructor_shouldThrowOnNullExtractor() {
        assertThrows(NullPointerException.class, () -> new SchemaSnapshotService(null));
    }
}
