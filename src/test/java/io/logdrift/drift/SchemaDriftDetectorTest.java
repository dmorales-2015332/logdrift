package io.logdrift.drift;

import io.logdrift.schema.LogSchema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SchemaDriftDetectorTest {

    private SchemaDriftDetector detector;

    @BeforeEach
    void setUp() {
        detector = new SchemaDriftDetector();
    }

    @Test
    void noEvents_whenSchemasAreIdentical() {
        LogSchema baseline  = schema(Map.of("level", "string", "ts", "long", "msg", "string"));
        LogSchema candidate = schema(Map.of("level", "string", "ts", "long", "msg", "string"));

        List<DriftEvent> events = detector.detect(baseline, candidate);

        assertTrue(events.isEmpty(), "Expected no drift events");
    }

    @Test
    void detectsFieldAdded() {
        LogSchema baseline  = schema(Map.of("level", "string", "msg", "string"));
        LogSchema candidate = schema(Map.of("level", "string", "msg", "string", "traceId", "string"));

        List<DriftEvent> events = detector.detect(baseline, candidate);

        assertEquals(1, events.size());
        DriftEvent e = events.get(0);
        assertEquals(DriftEvent.Type.FIELD_ADDED, e.getType());
        assertEquals("traceId", e.getFieldName());
        assertNull(e.getBaselineType());
        assertEquals("string", e.getCandidateType());
    }

    @Test
    void detectsFieldRemoved() {
        LogSchema baseline  = schema(Map.of("level", "string", "msg", "string", "duration", "long"));
        LogSchema candidate = schema(Map.of("level", "string", "msg", "string"));

        List<DriftEvent> events = detector.detect(baseline, candidate);

        assertEquals(1, events.size());
        DriftEvent e = events.get(0);
        assertEquals(DriftEvent.Type.FIELD_REMOVED, e.getType());
        assertEquals("duration", e.getFieldName());
        assertEquals("long", e.getBaselineType());
        assertNull(e.getCandidateType());
    }

    @Test
    void detectsTypeChanged() {
        LogSchema baseline  = schema(Map.of("ts", "long"));
        LogSchema candidate = schema(Map.of("ts", "string"));

        List<DriftEvent> events = detector.detect(baseline, candidate);

        assertEquals(1, events.size());
        DriftEvent e = events.get(0);
        assertEquals(DriftEvent.Type.TYPE_CHANGED, e.getType());
        assertEquals("ts", e.getFieldName());
        assertEquals("long", e.getBaselineType());
        assertEquals("string", e.getCandidateType());
    }

    @Test
    void throwsOnNullBaseline() {
        LogSchema candidate = schema(Map.of("level", "string"));
        assertThrows(IllegalArgumentException.class, () -> detector.detect(null, candidate));
    }

    @Test
    void throwsOnNullCandidate() {
        LogSchema baseline = schema(Map.of("level", "string"));
        assertThrows(IllegalArgumentException.class, () -> detector.detect(baseline, null));
    }

    // --- helpers ---

    private LogSchema schema(Map<String, String> fields) {
        return new LogSchema("test-service", fields);
    }
}
