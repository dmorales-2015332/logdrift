package io.logdrift.routing;

import io.logdrift.drift.DriftEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DriftEventRouterTest {

    private DriftEventRouter router;

    @BeforeEach
    void setUp() {
        router = new DriftEventRouter();
    }

    @Test
    void shouldRouteEventToMatchingServiceHandler() {
        List<DriftEvent> received = new ArrayList<>();
        router.registerServiceHandler("order-service", received::add);

        DriftEvent event = mock(DriftEvent.class);
        when(event.getServiceName()).thenReturn("order-service");

        router.route(event);

        assertEquals(1, received.size());
        assertSame(event, received.get(0));
    }

    @Test
    void shouldRouteEventToGlobalHandler() {
        List<DriftEvent> received = new ArrayList<>();
        router.registerGlobalHandler(received::add);

        DriftEvent event = mock(DriftEvent.class);
        when(event.getServiceName()).thenReturn("any-service");

        router.route(event);

        assertEquals(1, received.size());
    }

    @Test
    void shouldNotRouteToUnrelatedServiceHandler() {
        List<DriftEvent> received = new ArrayList<>();
        router.registerServiceHandler("payment-service", received::add);

        DriftEvent event = mock(DriftEvent.class);
        when(event.getServiceName()).thenReturn("order-service");

        router.route(event);

        assertTrue(received.isEmpty());
    }

    @Test
    void shouldRouteAllEvents() {
        List<DriftEvent> received = new ArrayList<>();
        router.registerGlobalHandler(received::add);

        DriftEvent e1 = mock(DriftEvent.class);
        DriftEvent e2 = mock(DriftEvent.class);
        when(e1.getServiceName()).thenReturn("svc-a");
        when(e2.getServiceName()).thenReturn("svc-b");

        router.routeAll(List.of(e1, e2));

        assertEquals(2, received.size());
    }

    @Test
    void shouldIgnoreNullEvent() {
        assertDoesNotThrow(() -> router.route(null));
    }

    @Test
    void shouldRejectNullHandler() {
        assertThrows(IllegalArgumentException.class, () -> router.registerGlobalHandler(null));
        assertThrows(IllegalArgumentException.class, () -> router.registerServiceHandler("svc", null));
    }

    @Test
    void shouldReportCorrectHandlerCounts() {
        router.registerServiceHandler("svc", e -> {});
        router.registerServiceHandler("svc", e -> {});
        router.registerGlobalHandler(e -> {});

        assertEquals(2, router.handlerCount("svc"));
        assertEquals(1, router.globalHandlerCount());
    }

    @Test
    void shouldClearAllHandlers() {
        router.registerServiceHandler("svc", e -> {});
        router.registerGlobalHandler(e -> {});
        router.clearAll();

        assertEquals(0, router.handlerCount("svc"));
        assertEquals(0, router.globalHandlerCount());
    }
}
