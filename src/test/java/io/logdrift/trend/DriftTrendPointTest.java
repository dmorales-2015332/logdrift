package io.logdrift.trend;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class DriftTrendPointTest {

    private static final Instant TS = Instant.parse("2024-06-01T10:00:00Z");

    @Test
    void constructor_validArgs_setsFields() {
        DriftTrendPoint point = new DriftTrendPoint(TS, 5, 2.5);
        assertEquals(TS, point.getTimestamp());
        assertEquals(5, point.getEventCount());
        assertEquals(2.5, point.getRollingRate());
    }

    @Test
    void constructor_nullTimestamp_throws() {
        assertThrows(NullPointerException.class,
                () -> new DriftTrendPoint(null, 0, 0.0));
    }

    @Test
    void constructor_negativeCount_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new DriftTrendPoint(TS, -1, 0.0));
    }

    @Test
    void equals_sameValues_returnsTrue() {
        DriftTrendPoint a = new DriftTrendPoint(TS, 3, 1.5);
        DriftTrendPoint b = new DriftTrendPoint(TS, 3, 1.5);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void equals_differentCount_returnsFalse() {
        DriftTrendPoint a = new DriftTrendPoint(TS, 3, 1.5);
        DriftTrendPoint b = new DriftTrendPoint(TS, 4, 1.5);
        assertNotEquals(a, b);
    }

    @Test
    void toString_containsExpectedFields() {
        DriftTrendPoint point = new DriftTrendPoint(TS, 7, 3.5);
        String str = point.toString();
        assertTrue(str.contains("7"));
        assertTrue(str.contains("3.50"));
    }
}
