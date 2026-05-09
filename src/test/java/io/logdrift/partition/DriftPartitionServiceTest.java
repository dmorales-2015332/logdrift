package io.logdrift.partition;

import io.logdrift.drift.DriftEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DriftPartitionServiceTest {

    private DriftPartitionStrategy strategy;
    private DriftPartitionService service;

    private DriftEvent eventA;
    private DriftEvent eventB;
    private DriftEvent eventC;

    @BeforeEach
    void setUp() {
        strategy = mock(DriftPartitionStrategy.class);
        service = new DriftPartitionService(strategy);

        eventA = mock(DriftEvent.class);
        eventB = mock(DriftEvent.class);
        eventC = mock(DriftEvent.class);

        when(strategy.resolvePartitionKey(eventA)).thenReturn("service-auth");
        when(strategy.resolvePartitionKey(eventB)).thenReturn("service-order");
        when(strategy.resolvePartitionKey(eventC)).thenReturn("service-auth");
    }

    @Test
    void constructor_nullStrategy_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> new DriftPartitionService(null));
    }

    @Test
    void partition_emptyList_returnsEmptyMap() {
        Map<String, List<DriftEvent>> result = service.partition(Collections.emptyList());
        assertTrue(result.isEmpty());
    }

    @Test
    void partition_nullList_returnsEmptyMap() {
        Map<String, List<DriftEvent>> result = service.partition(null);
        assertTrue(result.isEmpty());
    }

    @Test
    void partition_groupsEventsByStrategyKey() {
        List<DriftEvent> events = Arrays.asList(eventA, eventB, eventC);
        Map<String, List<DriftEvent>> result = service.partition(events);

        assertEquals(2, result.size());
        assertEquals(2, result.get("service-auth").size());
        assertEquals(1, result.get("service-order").size());
        assertTrue(result.get("service-auth").contains(eventA));
        assertTrue(result.get("service-auth").contains(eventC));
    }

    @Test
    void countPartitions_returnsDistinctKeyCount() {
        List<DriftEvent> events = Arrays.asList(eventA, eventB, eventC);
        assertEquals(2, service.countPartitions(events));
    }

    @Test
    void getPartition_knownLabel_returnsMatchingEvents() {
        List<DriftEvent> events = Arrays.asList(eventA, eventB, eventC);
        List<DriftEvent> authEvents = service.getPartition(events, "service-auth");
        assertEquals(2, authEvents.size());
    }

    @Test
    void getPartition_unknownLabel_returnsEmptyList() {
        List<DriftEvent> events = Arrays.asList(eventA, eventB);
        List<DriftEvent> result = service.getPartition(events, "service-unknown");
        assertTrue(result.isEmpty());
    }

    @Test
    void getStrategy_returnsInjectedStrategy() {
        assertSame(strategy, service.getStrategy());
    }
}
