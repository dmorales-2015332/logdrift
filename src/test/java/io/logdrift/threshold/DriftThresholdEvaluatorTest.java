package io.logdrift.threshold;

import io.logdrift.drift.DriftEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DriftThresholdEvaluatorTest {

    private DriftThresholdRegistry registry;
    private DriftThresholdEvaluator evaluator;

    @BeforeEach
    void setUp() {
        registry = new DriftThresholdRegistry();
        evaluator = new DriftThresholdEvaluator(registry);
    }

    private DriftEvent mockEvent(String serviceId, String driftType) {
        DriftEvent event = mock(DriftEvent.class);
        when(event.getServiceId()).thenReturn(serviceId);
        when(event.getDriftType()).thenReturn(driftType);
        return event;
    }

    @Test
    void evaluate_noThresholdRegistered_returnsEmpty() {
        DriftEvent event = mockEvent("svc-a", "FIELD_REMOVED");
        Optional<ThresholdViolation> result = evaluator.evaluate(event);
        assertTrue(result.isEmpty());
    }

    @Test
    void evaluate_belowLimit_returnsEmpty() {
        registry.register(new DriftThreshold("svc-a", "FIELD_REMOVED", 3, 60, false));
        DriftEvent event = mockEvent("svc-a", "FIELD_REMOVED");
        evaluator.evaluate(event);
        evaluator.evaluate(event);
        Optional<ThresholdViolation> result = evaluator.evaluate(event);
        assertTrue(result.isEmpty());
    }

    @Test
    void evaluate_exceedsLimit_returnsViolation() {
        registry.register(new DriftThreshold("svc-b", "TYPE_CHANGE", 2, 60, true));
        DriftEvent event = mockEvent("svc-b", "TYPE_CHANGE");
        evaluator.evaluate(event);
        evaluator.evaluate(event);
        Optional<ThresholdViolation> result = evaluator.evaluate(event);
        assertTrue(result.isPresent());
        ThresholdViolation v = result.get();
        assertEquals(3, v.getObservedCount());
        assertEquals(1, v.getExcess());
        assertTrue(v.isHardLimitViolation());
    }

    @Test
    void evaluate_afterReset_countsFromZero() {
        registry.register(new DriftThreshold("svc-c", "FIELD_ADDED", 1, 60, false));
        DriftEvent event = mockEvent("svc-c", "FIELD_ADDED");
        evaluator.evaluate(event);
        evaluator.evaluate(event); // violates
        evaluator.reset("svc-c", "FIELD_ADDED");
        Optional<ThresholdViolation> result = evaluator.evaluate(event);
        assertTrue(result.isEmpty());
    }

    @Test
    void constructor_nullRegistry_throws() {
        assertThrows(NullPointerException.class, () -> new DriftThresholdEvaluator(null));
    }

    @Test
    void evaluate_nullEvent_throws() {
        assertThrows(NullPointerException.class, () -> evaluator.evaluate(null));
    }
}
