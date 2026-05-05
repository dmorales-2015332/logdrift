package io.logdrift.compare;

import io.logdrift.drift.DriftEvent;
import io.logdrift.schema.LogSchema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SchemaComparatorTest {

    private SchemaComparator comparator;

    @BeforeEach
    void setUp() {
        comparator = new SchemaComparator();
    }

    @Test
    void shouldReturnEmptyListWhenSchemasAreIdentical() {
        LogSchema baseline  = schemaOf(Map.of("level", "string", "message", "string"));
        LogSchema candidate = schemaOf(Map.of("level", "string", "message", "string"));

        List<DriftEvent> events = comparator.compare(baseline, candidate, "svc-a");

        assertTrue(events.isEmpty(), "Identical schemas should produce no drift events");
    }

    @Test
    void shouldDetectAddedField() {
        LogSchema baseline  = schemaOf(Map.of("level", "string"));
        LogSchema candidate = schemaOf(Map.of("level", "string", "traceId", "string"));

        List<DriftEvent> events = comparator.compare(baseline, candidate, "svc-b");

        assertEquals(1, events.size());
        assertEquals(DriftEvent.Type.FIELD_ADDED, events.get(0).getType());
        assertEquals("traceId", events.get(0).getFieldName());
    }

    @Test
    void shouldDetectRemovedField() {
        LogSchema baseline  = schemaOf(Map.of("level", "string", "requestId", "string"));
        LogSchema candidate = schemaOf(Map.of("level", "string"));

        List<DriftEvent> events = comparator.compare(baseline, candidate, "svc-c");

        assertEquals(1, events.size());
        assertEquals(DriftEvent.Type.FIELD_REMOVED, events.get(0).getType());
        assertEquals("requestId", events.get(0).getFieldName());
    }

    @Test
    void shouldDetectTypeChange() {
        LogSchema baseline  = schemaOf(Map.of("duration", "string"));
        LogSchema candidate = schemaOf(Map.of("duration", "number"));

        List<DriftEvent> events = comparator.compare(baseline, candidate, "svc-d");

        assertEquals(1, events.size());
        assertEquals(DriftEvent.Type.TYPE_CHANGED, events.get(0).getType());
        assertEquals("duration", events.get(0).getFieldName());
    }

    @Test
    void shouldThrowWhenBaselineIsNull() {
        LogSchema candidate = schemaOf(Map.of("level", "string"));
        assertThrows(IllegalArgumentException.class,
            () -> comparator.compare(null, candidate, "svc-e"));
    }

    @Test
    void shouldAttributeEventsToCorrectService() {
        LogSchema baseline  = schemaOf(Map.of("level", "string"));
        LogSchema candidate = schemaOf(Map.of("level", "string", "env", "string"));

        List<DriftEvent> events = comparator.compare(baseline, candidate, "payments-service");

        assertFalse(events.isEmpty());
        events.forEach(e -> assertEquals("payments-service", e.getService()));
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private LogSchema schemaOf(Map<String, String> fields) {
        return new LogSchema(new HashMap<>(fields));
    }
}
