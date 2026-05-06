package io.logdrift.schedule;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class DriftScheduleConfigTest {

    @Test
    void defaultsAreApplied() {
        DriftScheduleConfig config = DriftScheduleConfig.builder("svc-x").build();
        assertEquals("svc-x", config.getServiceId());
        assertEquals(Duration.ofMinutes(5), config.getInterval());
        assertTrue(config.isAlertOnDrift());
        assertEquals(3, config.getMaxConsecutiveFailures());
    }

    @Test
    void customValuesAreStored() {
        DriftScheduleConfig config = DriftScheduleConfig.builder("svc-y")
                .interval(Duration.ofSeconds(30))
                .alertOnDrift(false)
                .maxConsecutiveFailures(5)
                .build();
        assertEquals(Duration.ofSeconds(30), config.getInterval());
        assertFalse(config.isAlertOnDrift());
        assertEquals(5, config.getMaxConsecutiveFailures());
    }

    @Test
    void nullServiceIdThrows() {
        assertThrows(NullPointerException.class, () ->
                DriftScheduleConfig.builder(null).build());
    }

    @Test
    void nullIntervalThrows() {
        assertThrows(NullPointerException.class, () ->
                DriftScheduleConfig.builder("svc-z").interval(null).build());
    }

    @Test
    void toStringContainsServiceId() {
        DriftScheduleConfig config = DriftScheduleConfig.builder("svc-toString").build();
        assertTrue(config.toString().contains("svc-toString"));
    }
}
