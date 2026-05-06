package io.logdrift.metrics;

import io.logdrift.drift.DriftEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DriftMetricsCollectorTest {

    private DriftMetricsCollector collector;

    @BeforeEach
    void setUp() {
        collector = new DriftMetricsCollector();
    }

    private DriftEvent mockEvent(String service, DriftEvent.DriftType type) {
        DriftEvent event = mock(DriftEvent.class);
        when(event.getServiceName()).thenReturn(service);
        when(event.getDriftType()).thenReturn(type);
        return event;
    }

    @Test
    void record_incrementsDriftCountForService() {
        collector.record(mockEvent("auth-service", DriftEvent.DriftType.FIELD_ADDED));
        collector.record(mockEvent("auth-service", DriftEvent.DriftType.FIELD_REMOVED));

        assertEquals(2L, collector.getDriftCountForService("auth-service"));
    }

    @Test
    void record_tracksMultipleServicesIndependently() {
        collector.record(mockEvent("auth-service", DriftEvent.DriftType.FIELD_ADDED));
        collector.record(mockEvent("order-service", DriftEvent.DriftType.TYPE_CHANGED));

        assertEquals(1L, collector.getDriftCountForService("auth-service"));
        assertEquals(1L, collector.getDriftCountForService("order-service"));
    }

    @Test
    void record_nullEventDoesNotThrow() {
        assertDoesNotThrow(() -> collector.record(null));
    }

    @Test
    void recordAll_aggregatesMultipleEvents() {
        List<DriftEvent> events = List.of(
                mockEvent("svc-a", DriftEvent.DriftType.FIELD_ADDED),
                mockEvent("svc-a", DriftEvent.DriftType.FIELD_REMOVED),
                mockEvent("svc-b", DriftEvent.DriftType.TYPE_CHANGED)
        );
        collector.recordAll(events);

        DriftMetricsSummary summary = collector.getSummary();
        assertEquals(3L, summary.getTotalDriftEvents());
        assertEquals(1L, summary.getTotalFieldAdditions());
        assertEquals(1L, summary.getTotalFieldRemovals());
        assertEquals(1L, summary.getTotalTypeChanges());
    }

    @Test
    void getSummary_identifiesMostDriftedService() {
        collector.record(mockEvent("noisy-service", DriftEvent.DriftType.FIELD_ADDED));
        collector.record(mockEvent("noisy-service", DriftEvent.DriftType.FIELD_REMOVED));
        collector.record(mockEvent("quiet-service", DriftEvent.DriftType.FIELD_ADDED));

        assertEquals("noisy-service", collector.getSummary().getMostDriftedService());
    }

    @Test
    void reset_clearsAllCounters() {
        collector.record(mockEvent("svc", DriftEvent.DriftType.FIELD_ADDED));
        collector.reset();

        DriftMetricsSummary summary = collector.getSummary();
        assertEquals(0L, summary.getTotalDriftEvents());
        assertEquals(0L, collector.getDriftCountForService("svc"));
    }

    @Test
    void getSummary_windowStartIsBeforeWindowEnd() {
        DriftMetricsSummary summary = collector.getSummary();
        assertFalse(summary.getWindowStart().isAfter(summary.getWindowEnd()));
    }
}
