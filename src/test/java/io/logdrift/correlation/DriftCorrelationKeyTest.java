package io.logdrift.correlation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DriftCorrelationKeyTest {

    @Test
    void equalKeys_areEqual() {
        DriftCorrelationKey k1 = new DriftCorrelationKey("svc-a", "userId", "TYPE_CHANGE");
        DriftCorrelationKey k2 = new DriftCorrelationKey("svc-a", "userId", "TYPE_CHANGE");
        assertEquals(k1, k2);
        assertEquals(k1.hashCode(), k2.hashCode());
    }

    @Test
    void differentServiceId_notEqual() {
        DriftCorrelationKey k1 = new DriftCorrelationKey("svc-a", "userId", "TYPE_CHANGE");
        DriftCorrelationKey k2 = new DriftCorrelationKey("svc-b", "userId", "TYPE_CHANGE");
        assertNotEquals(k1, k2);
    }

    @Test
    void differentFieldName_notEqual() {
        DriftCorrelationKey k1 = new DriftCorrelationKey("svc-a", "userId", "TYPE_CHANGE");
        DriftCorrelationKey k2 = new DriftCorrelationKey("svc-a", "orderId", "TYPE_CHANGE");
        assertNotEquals(k1, k2);
    }

    @Test
    void nullServiceId_throwsException() {
        assertThrows(NullPointerException.class,
                () -> new DriftCorrelationKey(null, "userId", "TYPE_CHANGE"));
    }

    @Test
    void toString_containsAllFields() {
        DriftCorrelationKey key = new DriftCorrelationKey("svc-a", "userId", "TYPE_CHANGE");
        String str = key.toString();
        assertTrue(str.contains("svc-a"));
        assertTrue(str.contains("userId"));
        assertTrue(str.contains("TYPE_CHANGE"));
    }
}
