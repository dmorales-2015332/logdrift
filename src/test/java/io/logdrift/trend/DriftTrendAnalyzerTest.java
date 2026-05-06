package io.logdrift.trend;

import io.logdrift.drift.DriftEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DriftTrendAnalyzerTest {

    private DriftTrendAnalyzer analyzer;
    private static final Instant BASE = Instant.parse("2024-06-01T00:00:00Z");

    @BeforeEach
    void setUp() {
        analyzer = new DriftTrendAnalyzer();
    }

    @Test
    void analyze_emptyEvents_returnsZeroTrend() {
        DriftTrend trend = analyzer.analyze("svc-a", List.of(),
                BASE, BASE.plus(Duration.ofHours(2)), Duration.ofHours(1));
        assertEquals(0, trend.getTotalDriftEvents());
        assertEquals(0.0, trend.getDriftRate());
        assertEquals(DriftTrend.TrendDirection.STABLE, trend.getDirection());
    }

    @Test
    void analyze_eventsOutsideWindow_areExcluded() {
        List<DriftEvent> events = List.of(
                mockEvent("svc-a", BASE.minus(Duration.ofHours(1))),
                mockEvent("svc-a", BASE.plus(Duration.ofHours(3)))
        );
        DriftTrend trend = analyzer.analyze("svc-a", events,
                BASE, BASE.plus(Duration.ofHours(2)), Duration.ofHours(1));
        assertEquals(0, trend.getTotalDriftEvents());
    }

    @Test
    void analyze_increasingTrend_detectedCorrectly() {
        List<DriftEvent> events = new ArrayList<>();
        // 1 event in first hour, 5 in second hour
        events.add(mockEvent("svc-b", BASE.plus(Duration.ofMinutes(10))));
        for (int i = 0; i < 5; i++) {
            events.add(mockEvent("svc-b", BASE.plus(Duration.ofMinutes(70 + i * 5))));
        }
        DriftTrend trend = analyzer.analyze("svc-b", events,
                BASE, BASE.plus(Duration.ofHours(2)), Duration.ofHours(1));
        assertEquals(DriftTrend.TrendDirection.INCREASING, trend.getDirection());
        assertEquals(6, trend.getTotalDriftEvents());
    }

    @Test
    void analyze_decreasingTrend_detectedCorrectly() {
        List<DriftEvent> events = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            events.add(mockEvent("svc-c", BASE.plus(Duration.ofMinutes(5 + i * 5))));
        }
        events.add(mockEvent("svc-c", BASE.plus(Duration.ofMinutes(70))));
        DriftTrend trend = analyzer.analyze("svc-c", events,
                BASE, BASE.plus(Duration.ofHours(2)), Duration.ofHours(1));
        assertEquals(DriftTrend.TrendDirection.DECREASING, trend.getDirection());
    }

    @Test
    void analyze_dataPointsMatchBuckets() {
        DriftTrend trend = analyzer.analyze("svc-d", List.of(),
                BASE, BASE.plus(Duration.ofHours(4)), Duration.ofHours(1));
        assertEquals(4, trend.getDataPoints().size());
        assertEquals(BASE, trend.getDataPoints().get(0).getTimestamp());
    }

    @Test
    void analyze_serviceIdPreserved() {
        DriftTrend trend = analyzer.analyze("my-service", List.of(),
                BASE, BASE.plus(Duration.ofHours(1)), Duration.ofHours(1));
        assertEquals("my-service", trend.getServiceId());
    }

    private DriftEvent mockEvent(String serviceId, Instant timestamp) {
        return new DriftEvent(serviceId, "field.missing", "MISSING_FIELD", timestamp);
    }
}
