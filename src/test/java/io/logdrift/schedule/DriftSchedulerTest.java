package io.logdrift.schedule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DriftSchedulerTest {

    private List<DriftScheduleEntry> executed;
    private List<DriftScheduleEntry> limitBreached;
    private DriftScheduler scheduler;

    @BeforeEach
    void setUp() {
        executed = new ArrayList<>();
        limitBreached = new ArrayList<>();
        scheduler = new DriftScheduler(executed::add, limitBreached::add);
    }

    @Test
    void registerAndTickExecutesEntry() {
        DriftScheduleConfig config = DriftScheduleConfig.builder("svc-a")
                .interval(Duration.ofMinutes(1)).build();
        scheduler.register(config, Instant.now().minusSeconds(1));
        scheduler.tick();
        assertEquals(1, executed.size());
        assertEquals("svc-a", executed.get(0).getConfig().getServiceId());
    }

    @Test
    void entryNotDueIsSkipped() {
        DriftScheduleConfig config = DriftScheduleConfig.builder("svc-b")
                .interval(Duration.ofHours(1)).build();
        scheduler.register(config, Instant.now().plusSeconds(3600));
        scheduler.tick();
        assertTrue(executed.isEmpty());
    }

    @Test
    void failureIncreasesConsecutiveCount() {
        DriftScheduler failingScheduler = new DriftScheduler(
                e -> { throw new RuntimeException("simulated failure"); },
                limitBreached::add
        );
        DriftScheduleConfig config = DriftScheduleConfig.builder("svc-c")
                .interval(Duration.ofSeconds(1)).maxConsecutiveFailures(2).build();
        failingScheduler.register(config, Instant.now().minusSeconds(1));
        failingScheduler.tick();
        DriftScheduleEntry entry = failingScheduler.getEntry("svc-c").orElseThrow();
        assertEquals(1, entry.getConsecutiveFailures());
        assertEquals(DriftScheduleEntry.Status.FAILED, entry.getStatus());
    }

    @Test
    void failureLimitHandlerTriggeredWhenLimitExceeded() {
        DriftScheduler failingScheduler = new DriftScheduler(
                e -> { throw new RuntimeException("fail"); },
                limitBreached::add
        );
        DriftScheduleConfig config = DriftScheduleConfig.builder("svc-d")
                .interval(Duration.ofMillis(1)).maxConsecutiveFailures(1).build();
        failingScheduler.register(config, Instant.now().minusSeconds(1));
        failingScheduler.tick();
        assertEquals(1, limitBreached.size());
    }

    @Test
    void unregisterRemovesEntry() {
        DriftScheduleConfig config = DriftScheduleConfig.builder("svc-e").build();
        scheduler.register(config);
        assertTrue(scheduler.unregister("svc-e"));
        assertEquals(0, scheduler.size());
        assertFalse(scheduler.unregister("svc-e"));
    }

    @Test
    void getAllEntriesReturnsAll() {
        scheduler.register(DriftScheduleConfig.builder("svc-f").build());
        scheduler.register(DriftScheduleConfig.builder("svc-g").build());
        assertEquals(2, scheduler.getAllEntries().size());
    }
}
