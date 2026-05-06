package io.logdrift.trend;

import io.logdrift.drift.DriftEvent;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Analyzes a sequence of DriftEvents to compute trend metrics for a service.
 */
public class DriftTrendAnalyzer {

    private static final double STABILITY_THRESHOLD = 0.1; // 10% change considered stable

    /**
     * Analyzes drift events within the given window and produces a DriftTrend.
     *
     * @param serviceId   the service being analyzed
     * @param events      all drift events for the service (may span beyond window)
     * @param windowStart start of the analysis window
     * @param windowEnd   end of the analysis window
     * @param bucketSize  duration of each time bucket for data points
     * @return computed DriftTrend
     */
    public DriftTrend analyze(String serviceId, List<DriftEvent> events,
                              Instant windowStart, Instant windowEnd,
                              Duration bucketSize) {
        Objects.requireNonNull(serviceId);
        Objects.requireNonNull(events);
        Objects.requireNonNull(windowStart);
        Objects.requireNonNull(windowEnd);
        Objects.requireNonNull(bucketSize);

        List<DriftEvent> windowed = events.stream()
                .filter(e -> !e.getTimestamp().isBefore(windowStart)
                        && e.getTimestamp().isBefore(windowEnd))
                .sorted(Comparator.comparing(DriftEvent::getTimestamp))
                .collect(Collectors.toList());

        List<DriftTrendPoint> points = buildDataPoints(windowed, windowStart, windowEnd, bucketSize);

        double windowHours = Duration.between(windowStart, windowEnd).toMinutes() / 60.0;
        double driftRate = windowHours > 0 ? windowed.size() / windowHours : 0.0;

        DriftTrend.TrendDirection direction = computeDirection(points);

        return new DriftTrend(serviceId, windowStart, windowEnd,
                windowed.size(), driftRate, direction, points);
    }

    private List<DriftTrendPoint> buildDataPoints(List<DriftEvent> events,
                                                   Instant windowStart, Instant windowEnd,
                                                   Duration bucketSize) {
        List<DriftTrendPoint> points = new ArrayList<>();
        Instant cursor = windowStart;
        while (cursor.isBefore(windowEnd)) {
            Instant bucketEnd = cursor.plus(bucketSize);
            final Instant bucketStart = cursor;
            long count = events.stream()
                    .filter(e -> !e.getTimestamp().isBefore(bucketStart)
                            && e.getTimestamp().isBefore(bucketEnd))
                    .count();
            double bucketHours = bucketSize.toMinutes() / 60.0;
            double rate = bucketHours > 0 ? count / bucketHours : 0.0;
            points.add(new DriftTrendPoint(cursor, (int) count, rate));
            cursor = bucketEnd;
        }
        return points;
    }

    private DriftTrend.TrendDirection computeDirection(List<DriftTrendPoint> points) {
        if (points.size() < 2) return DriftTrend.TrendDirection.STABLE;
        int half = points.size() / 2;
        double firstHalfAvg = points.subList(0, half).stream()
                .mapToDouble(DriftTrendPoint::getRollingRate).average().orElse(0);
        double secondHalfAvg = points.subList(half, points.size()).stream()
                .mapToDouble(DriftTrendPoint::getRollingRate).average().orElse(0);
        if (firstHalfAvg == 0) return DriftTrend.TrendDirection.STABLE;
        double change = (secondHalfAvg - firstHalfAvg) / firstHalfAvg;
        if (change > STABILITY_THRESHOLD) return DriftTrend.TrendDirection.INCREASING;
        if (change < -STABILITY_THRESHOLD) return DriftTrend.TrendDirection.DECREASING;
        return DriftTrend.TrendDirection.STABLE;
    }
}
