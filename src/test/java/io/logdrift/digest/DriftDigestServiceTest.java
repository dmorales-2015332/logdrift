package io.logdrift.digest;

import io.logdrift.drift.DriftEvent;
import io.logdrift.metrics.DriftMetricsSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DriftDigestServiceTest {

    private DriftDigestService digestService;
    private final Instant from = Instant.parse("2024-06-01T00:00:00Z");
    private final Instant to   = Instant.parse("2024-06-01T23:59:59Z");

    @BeforeEach
    void setUp() {
        digestService = new DriftDigestService();
    }

    @Test
    void buildDigest_emptyEvents_returnsEmptyDigest() {
        DriftDigest digest = digestService.buildDigest("auth-service", List.of(), from, to);
        assertTrue(digest.isEmpty());
        assertEquals(0, digest.getTotalDrifts());
        assertEquals("auth-service", digest.getService());
    }

    @Test
    void buildDigest_nullEvents_returnsEmptyDigest() {
        DriftDigest digest = digestService.buildDigest("auth-service", null, from, to);
        assertTrue(digest.isEmpty());
    }

    @Test
    void buildDigest_countsEventsInWindow() {
        DriftEvent e1 = mock(DriftEvent.class);
        when(e1.getDetectedAt()).thenReturn(Instant.parse("2024-06-01T10:00:00Z"));
        when(e1.getAddedFields()).thenReturn(Set.of("traceId", "spanId"));
        when(e1.getRemovedFields()).thenReturn(Set.of());
        when(e1.getDriftType()).thenReturn("FIELD_ADDED");

        DriftEvent e2 = mock(DriftEvent.class);
        when(e2.getDetectedAt()).thenReturn(Instant.parse("2024-06-01T15:00:00Z"));
        when(e2.getAddedFields()).thenReturn(Set.of());
        when(e2.getRemovedFields()).thenReturn(Set.of("userId"));
        when(e2.getDriftType()).thenReturn("FIELD_REMOVED");

        DriftDigest digest = digestService.buildDigest("order-service", List.of(e1, e2), from, to);

        assertEquals(2, digest.getTotalDrifts());
        assertEquals(2, digest.getTotalFieldsAdded());
        assertEquals(1, digest.getTotalFieldsRemoved());
        assertEquals(1L, digest.getDriftsByType().get("FIELD_ADDED"));
        assertEquals(1L, digest.getDriftsByType().get("FIELD_REMOVED"));
    }

    @Test
    void buildDigest_excludesEventsOutsideWindow() {
        DriftEvent outside = mock(DriftEvent.class);
        when(outside.getDetectedAt()).thenReturn(Instant.parse("2024-05-31T23:59:00Z"));
        when(outside.getAddedFields()).thenReturn(Set.of());
        when(outside.getRemovedFields()).thenReturn(Set.of());
        when(outside.getDriftType()).thenReturn("FIELD_ADDED");

        DriftDigest digest = digestService.buildDigest("payment-service", List.of(outside), from, to);
        assertTrue(digest.isEmpty());
    }

    @Test
    void buildDigestFromSummary_nullSummary_returnsEmptyDigest() {
        DriftDigest digest = digestService.buildDigestFromSummary("svc", null, from, to);
        assertTrue(digest.isEmpty());
    }

    @Test
    void buildDigestFromSummary_populatesFieldsFromSummary() {
        DriftMetricsSummary summary = mock(DriftMetricsSummary.class);
        when(summary.getTotalDriftsDetected()).thenReturn(5L);
        when(summary.getTotalFieldsAdded()).thenReturn(3L);
        when(summary.getTotalFieldsRemoved()).thenReturn(2L);

        DriftDigest digest = digestService.buildDigestFromSummary("inventory-service", summary, from, to);

        assertEquals(5L, digest.getTotalDrifts());
        assertEquals(3L, digest.getTotalFieldsAdded());
        assertEquals(2L, digest.getTotalFieldsRemoved());
        assertFalse(digest.isEmpty());
    }
}
