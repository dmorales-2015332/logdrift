package io.logdrift.filter;

import io.logdrift.drift.DriftEvent;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DriftEventFilterTest {

    private DriftEvent event(String service, String field, DriftEvent.Severity severity) {
        return new DriftEvent(service, field, severity, "desc");
    }

    @Test
    void shouldReturnAllEventsWhenCriteriaIsEmpty() {
        FilterCriteria criteria = FilterCriteria.builder().build();
        DriftEventFilter filter = new DriftEventFilter(criteria);

        List<DriftEvent> events = List.of(
                event("svc-a", "timestamp", DriftEvent.Severity.WARNING),
                event("svc-b", "userId", DriftEvent.Severity.CRITICAL)
        );

        List<DriftEvent> result = filter.apply(events);
        assertEquals(2, result.size());
    }

    @Test
    void shouldFilterBySeverity() {
        FilterCriteria criteria = FilterCriteria.builder()
                .allowSeverity(DriftEvent.Severity.CRITICAL)
                .build();
        DriftEventFilter filter = new DriftEventFilter(criteria);

        List<DriftEvent> events = List.of(
                event("svc-a", "timestamp", DriftEvent.Severity.WARNING),
                event("svc-b", "userId", DriftEvent.Severity.CRITICAL)
        );

        List<DriftEvent> result = filter.apply(events);
        assertEquals(1, result.size());
        assertEquals(DriftEvent.Severity.CRITICAL, result.get(0).getSeverity());
    }

    @Test
    void shouldFilterByServiceNamePattern() {
        FilterCriteria criteria = FilterCriteria.builder()
                .serviceNamePattern("svc-a.*")
                .build();
        DriftEventFilter filter = new DriftEventFilter(criteria);

        List<DriftEvent> events = List.of(
                event("svc-a-payments", "amount", DriftEvent.Severity.WARNING),
                event("svc-b-auth", "token", DriftEvent.Severity.WARNING)
        );

        List<DriftEvent> result = filter.apply(events);
        assertEquals(1, result.size());
        assertEquals("svc-a-payments", result.get(0).getServiceName());
    }

    @Test
    void shouldExcludeSpecifiedFields() {
        FilterCriteria criteria = FilterCriteria.builder()
                .excludeField("internalDebug")
                .build();
        DriftEventFilter filter = new DriftEventFilter(criteria);

        List<DriftEvent> events = List.of(
                event("svc-a", "internalDebug", DriftEvent.Severity.WARNING),
                event("svc-a", "userId", DriftEvent.Severity.WARNING)
        );

        List<DriftEvent> result = filter.apply(events);
        assertEquals(1, result.size());
        assertEquals("userId", result.get(0).getFieldName());
    }

    @Test
    void shouldReturnEmptyListForNullInput() {
        FilterCriteria criteria = FilterCriteria.builder().build();
        DriftEventFilter filter = new DriftEventFilter(criteria);
        assertTrue(filter.apply(null).isEmpty());
    }

    @Test
    void shouldThrowWhenCriteriaIsNull() {
        assertThrows(IllegalArgumentException.class, () -> new DriftEventFilter(null));
    }
}
