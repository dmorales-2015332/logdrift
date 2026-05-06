package io.logdrift.correlation;

import io.logdrift.drift.DriftEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DriftCorrelationServiceTest {

    private DriftCorrelationService service;

    @BeforeEach
    void setUp() {
        service = new DriftCorrelationService();
    }

    private DriftEvent event(String serviceId, String fieldName, String driftType) {
        return new DriftEvent(serviceId, fieldName, driftType, Instant.now());
    }

    @Test
    void correlate_groupsEventsByKey() {
        service.correlate(Arrays.asList(
                event("svc-a", "userId", "TYPE_CHANGE"),
                event("svc-a", "userId", "TYPE_CHANGE"),
                event("svc-b", "userId", "TYPE_CHANGE")
        ));

        List<DriftCorrelationGroup> groups = service.getGroups();
        assertEquals(2, groups.size());
    }

    @Test
    void getRecurringGroups_returnsOnlyGroupsWithMultipleEvents() {
        service.correlate(Arrays.asList(
                event("svc-a", "userId", "TYPE_CHANGE"),
                event("svc-a", "userId", "TYPE_CHANGE"),
                event("svc-b", "orderId", "FIELD_REMOVED")
        ));

        List<DriftCorrelationGroup> recurring = service.getRecurringGroups();
        assertEquals(1, recurring.size());
        assertEquals("svc-a", recurring.get(0).getKey().getServiceId());
    }

    @Test
    void getCrossServiceGroups_returnsGroupsSpanningMultipleServices() {
        service.correlate(Arrays.asList(
                event("svc-a", "userId", "TYPE_CHANGE"),
                event("svc-b", "userId", "TYPE_CHANGE"),
                event("svc-c", "orderId", "FIELD_REMOVED")
        ));

        List<DriftCorrelationGroup> crossService = service.getCrossServiceGroups();
        assertEquals(2, crossService.size());
        crossService.forEach(g -> assertEquals("userId", g.getKey().getFieldName()));
    }

    @Test
    void reset_clearsAllGroups() {
        service.correlate(List.of(event("svc-a", "userId", "TYPE_CHANGE")));
        service.reset();
        assertTrue(service.getGroups().isEmpty());
    }

    @Test
    void correlate_emptyList_producesNoGroups() {
        service.correlate(List.of());
        assertTrue(service.getGroups().isEmpty());
    }

    @Test
    void group_tracksFirstAndLastSeen() throws InterruptedException {
        Instant before = Instant.now();
        Thread.sleep(5);
        service.correlate(Arrays.asList(
                event("svc-a", "userId", "TYPE_CHANGE"),
                event("svc-a", "userId", "TYPE_CHANGE")
        ));
        DriftCorrelationGroup group = service.getGroups().get(0);
        assertNotNull(group.getFirstSeen());
        assertNotNull(group.getLastSeen());
        assertFalse(group.getFirstSeen().isAfter(group.getLastSeen()));
    }
}
