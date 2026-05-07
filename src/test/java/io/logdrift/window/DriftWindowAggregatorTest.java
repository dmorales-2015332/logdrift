package io.logdrift.window;

import io.logdrift.drift.DriftEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class DriftWindowAggregatorTest {

    private static final String SERVICE = "order-service";
    private DriftWindowAggregator aggregator;
    private Instant base;

    @BeforeEach
    void setUp() {
        DriftWindowConfig config = DriftWindowConfig.tumbling(Duration.ofMinutes(5), SERVICE);
        aggregator = new DriftWindowAggregator(config);
        base = Instant.parse("2024-06-01T10:00:00Z");
    }

    @Test
    void recordCreatesNewBucketForFirstEvent() {
        DriftEvent event = mock(DriftEvent.class);
        aggregator.record(event, base);

        List<DriftWindowBucket> active = aggregator.getActiveBuckets();
        assertEquals(1, active.size());
        assertEquals(1, active.get(0).getEventCount());
    }

    @Test
    void multipleEventsInSameWindowGoToSameBucket() {
        DriftEvent e1 = mock(DriftEvent.class);
        DriftEvent e2 = mock(DriftEvent.class);
        aggregator.record(e1, base);
        aggregator.record(e2, base.plusSeconds(30));

        List<DriftWindowBucket> active = aggregator.getActiveBuckets();
        assertEquals(1, active.size());
        assertEquals(2, active.get(0).getEventCount());
    }

    @Test
    void drainCompletedReturnsBucketsAfterWindowExpires() {
        DriftEvent event = mock(DriftEvent.class);
        aggregator.record(event, base);

        Instant afterWindow = base.plus(Duration.ofMinutes(6));
        List<DriftWindowBucket> completed = aggregator.drainCompleted(afterWindow);

        assertEquals(1, completed.size());
        assertEquals(1, completed.get(0).getEventCount());
        assertTrue(aggregator.getActiveBuckets().isEmpty());
    }

    @Test
    void drainCompletedDoesNotReturnActiveBuckets() {
        DriftEvent event = mock(DriftEvent.class);
        aggregator.record(event, base);

        List<DriftWindowBucket> completed = aggregator.drainCompleted(base.plusSeconds(10));
        assertTrue(completed.isEmpty());
        assertEquals(1, aggregator.getActiveBuckets().size());
    }

    @Test
    void configIsRetained() {
        assertEquals(SERVICE, aggregator.getConfig().getServiceId());
        assertEquals(DriftWindowConfig.WindowType.TUMBLING, aggregator.getConfig().getWindowType());
    }

    @Test
    void bucketAcceptsEventWithinRange() {
        Instant start = base;
        Instant end = base.plus(Duration.ofMinutes(5));
        DriftWindowBucket bucket = new DriftWindowBucket(start, end);

        assertTrue(bucket.accepts(base));
        assertTrue(bucket.accepts(base.plusSeconds(299)));
        assertFalse(bucket.accepts(end));
        assertFalse(bucket.accepts(base.minusSeconds(1)));
    }
}
