package io.logdrift.rollup;

import io.logdrift.drift.DriftEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DriftRollupServiceTest {

    private DriftRollupService service;

    @BeforeEach
    void setUp() {
        service = new DriftRollupService(ChronoUnit.HOURS);
    }

    @Test
    void rollup_emptyList_returnsEmpty() {
        List<DriftRollupEntry> result = service.rollup(List.of());
        assertTrue(result.isEmpty());
    }

    @Test
    void rollup_nullList_returnsEmpty() {
        List<DriftRollupEntry> result = service.rollup(null);
        assertTrue(result.isEmpty());
    }

    @Test
    void rollup_singleEvent_producesOneBucket() {
        DriftEvent event = driftEvent(Instant.parse("2024-06-01T10:15:00Z"),
                Set.of("newField"), Set.of(), Set.of());

        List<DriftRollupEntry> result = service.rollup(List.of(event));

        assertEquals(1, result.size());
        DriftRollupEntry entry = result.get(0);
        assertEquals(Instant.parse("2024-06-01T10:00:00Z"), entry.getBucketStart());
        assertEquals(1, entry.getEventCount());
        assertEquals(1, entry.getTotalAddedFields());
        assertEquals(0, entry.getTotalRemovedFields());
        assertEquals(0, entry.getTotalTypeChanges());
    }

    @Test
    void rollup_eventsInSameBucket_aggregated() {
        DriftEvent e1 = driftEvent(Instant.parse("2024-06-01T10:05:00Z"),
                Set.of("a"), Set.of("b"), Set.of());
        DriftEvent e2 = driftEvent(Instant.parse("2024-06-01T10:45:00Z"),
                Set.of("c"), Set.of(), Set.of("d"));

        List<DriftRollupEntry> result = service.rollup(List.of(e1, e2));

        assertEquals(1, result.size());
        DriftRollupEntry entry = result.get(0);
        assertEquals(2, entry.getEventCount());
        assertEquals(2, entry.getTotalAddedFields());
        assertEquals(1, entry.getTotalRemovedFields());
        assertEquals(1, entry.getTotalTypeChanges());
        assertEquals(4, entry.getTotalChanges());
    }

    @Test
    void rollup_eventsInDifferentBuckets_separateEntries() {
        DriftEvent e1 = driftEvent(Instant.parse("2024-06-01T09:00:00Z"),
                Set.of("x"), Set.of(), Set.of());
        DriftEvent e2 = driftEvent(Instant.parse("2024-06-01T11:00:00Z"),
                Set.of(), Set.of("y"), Set.of());

        List<DriftRollupEntry> result = service.rollup(List.of(e1, e2));

        assertEquals(2, result.size());
        assertEquals(Instant.parse("2024-06-01T09:00:00Z"), result.get(0).getBucketStart());
        assertEquals(Instant.parse("2024-06-01T11:00:00Z"), result.get(1).getBucketStart());
    }

    @Test
    void constructor_nullBucketUnit_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> new DriftRollupService(null));
    }

    private DriftEvent driftEvent(Instant timestamp, Set<String> added,
                                   Set<String> removed, Set<String> typeChanges) {
        return DriftEvent.builder()
                .timestamp(timestamp)
                .addedFields(added)
                .removedFields(removed)
                .typeChanges(typeChanges)
                .build();
    }
}
