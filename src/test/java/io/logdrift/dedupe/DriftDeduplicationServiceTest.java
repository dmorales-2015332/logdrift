package io.logdrift.dedupe;

import io.logdrift.drift.DriftEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class DriftDeduplicationServiceTest {

    private DriftDeduplicationService service;

    @BeforeEach
    void setUp() {
        service = new DriftDeduplicationService(Duration.ofMinutes(5), 100);
    }

    @Test
    void shouldAllowNewUniqueEvent() {
        DriftEvent event = mockEvent("auth-service", "userId", "TYPE_CHANGED");
        boolean result = service.isNew(event, Instant.now());
        assertThat(result).isTrue();
    }

    @Test
    void shouldDeduplicateSameEventWithinWindow() {
        DriftEvent event = mockEvent("auth-service", "userId", "TYPE_CHANGED");
        Instant now = Instant.now();
        service.isNew(event, now);
        boolean second = service.isNew(event, now.plusSeconds(30));
        assertThat(second).isFalse();
    }

    @Test
    void shouldAllowSameEventAfterWindowExpires() {
        DriftEvent event = mockEvent("auth-service", "userId", "TYPE_CHANGED");
        Instant now = Instant.now();
        service.isNew(event, now);
        boolean afterExpiry = service.isNew(event, now.plus(Duration.ofMinutes(6)));
        assertThat(afterExpiry).isTrue();
    }

    @Test
    void shouldFilterDuplicatesFromList() {
        DriftEvent e1 = mockEvent("svc-a", "field1", "FIELD_REMOVED");
        DriftEvent e2 = mockEvent("svc-a", "field1", "FIELD_REMOVED");
        DriftEvent e3 = mockEvent("svc-b", "field2", "FIELD_ADDED");

        List<DriftEvent> result = service.deduplicate(List.of(e1, e2, e3));
        assertThat(result).hasSize(2);
    }

    @Test
    void shouldTrackCacheSize() {
        service.isNew(mockEvent("svc-a", "f1", "TYPE_CHANGED"), Instant.now());
        service.isNew(mockEvent("svc-b", "f2", "FIELD_ADDED"), Instant.now());
        assertThat(service.cacheSize()).isEqualTo(2);
    }

    @Test
    void shouldClearCache() {
        service.isNew(mockEvent("svc-a", "f1", "TYPE_CHANGED"), Instant.now());
        service.clearCache();
        assertThat(service.cacheSize()).isZero();
    }

    @Test
    void shouldRejectNullEventsInList() {
        assertThatNullPointerException()
                .isThrownBy(() -> service.deduplicate(null));
    }

    @Test
    void shouldRejectInvalidWindow() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new DriftDeduplicationService(Duration.ZERO, 100));
    }

    @Test
    void shouldRejectInvalidCacheSize() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new DriftDeduplicationService(Duration.ofMinutes(1), 0));
    }

    private DriftEvent mockEvent(String service, String field, String type) {
        DriftEvent event = mock(DriftEvent.class);
        when(event.getServiceName()).thenReturn(service);
        when(event.getFieldName()).thenReturn(field);
        when(event.getDriftType()).thenReturn(type);
        return event;
    }
}
