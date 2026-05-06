package io.logdrift.suppress;

import io.logdrift.drift.DriftEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class SuppressionAwareDriftFilterTest {

    private DriftSuppressionRegistry registry;
    private SuppressionAwareDriftFilter filter;
    private final Instant future = Instant.now().plus(2, ChronoUnit.HOURS);

    @BeforeEach
    void setUp() {
        registry = new DriftSuppressionRegistry();
        filter = new SuppressionAwareDriftFilter(registry);
    }

    private DriftEvent event(String service, String field) {
        DriftEvent e = mock(DriftEvent.class);
        when(e.getServiceName()).thenReturn(service);
        when(e.getFieldName()).thenReturn(field);
        return e;
    }

    @Test
    void filterSuppressed_removesMatchingEvents() {
        registry.register("order-service", "userId", future, "deploy", "alice");
        List<DriftEvent> events = List.of(
                event("order-service", "userId"),
                event("order-service", "timestamp"),
                event("payment-service", "userId")
        );
        List<DriftEvent> result = filter.filterSuppressed(events);
        assertThat(result).hasSize(2);
    }

    @Test
    void filterSuppressed_returnsAllEvents_whenNoSuppressions() {
        List<DriftEvent> events = List.of(
                event("svc-a", "field1"),
                event("svc-b", "field2")
        );
        assertThat(filter.filterSuppressed(events)).hasSize(2);
    }

    @Test
    void summarize_countsPassedAndSuppressedCorrectly() {
        registry.register("order-service", "userId", future, "test", "bob");
        List<DriftEvent> events = List.of(
                event("order-service", "userId"),
                event("order-service", "sessionId")
        );
        SuppressionAwareDriftFilter.SuppressionSummary summary = filter.summarize(events);
        assertThat(summary.suppressed()).isEqualTo(1);
        assertThat(summary.passed()).isEqualTo(1);
        assertThat(summary.total()).isEqualTo(2);
    }

    @Test
    void isSuppressed_singleEvent_returnsTrueWhenMatched() {
        registry.register("audit-service", "*", future, "maintenance", "carol");
        DriftEvent e = event("audit-service", "anyField");
        assertThat(filter.isSuppressed(e)).isTrue();
    }
}
