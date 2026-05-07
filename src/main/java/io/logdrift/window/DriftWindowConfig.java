package io.logdrift.window;

import java.time.Duration;
import java.util.Objects;

/**
 * Configuration for a sliding or tumbling time window used to
 * aggregate drift events over a fixed period.
 */
public class DriftWindowConfig {

    public enum WindowType { TUMBLING, SLIDING }

    private final WindowType windowType;
    private final Duration windowSize;
    private final Duration slideInterval;
    private final String serviceId;

    public DriftWindowConfig(WindowType windowType, Duration windowSize,
                             Duration slideInterval, String serviceId) {
        this.windowType = Objects.requireNonNull(windowType, "windowType must not be null");
        this.windowSize = Objects.requireNonNull(windowSize, "windowSize must not be null");
        this.slideInterval = slideInterval;
        this.serviceId = Objects.requireNonNull(serviceId, "serviceId must not be null");
    }

    public static DriftWindowConfig tumbling(Duration windowSize, String serviceId) {
        return new DriftWindowConfig(WindowType.TUMBLING, windowSize, windowSize, serviceId);
    }

    public static DriftWindowConfig sliding(Duration windowSize, Duration slideInterval,
                                            String serviceId) {
        return new DriftWindowConfig(WindowType.SLIDING, windowSize, slideInterval, serviceId);
    }

    public WindowType getWindowType() { return windowType; }
    public Duration getWindowSize() { return windowSize; }
    public Duration getSlideInterval() { return slideInterval; }
    public String getServiceId() { return serviceId; }

    @Override
    public String toString() {
        return "DriftWindowConfig{type=" + windowType +
               ", size=" + windowSize +
               ", slide=" + slideInterval +
               ", service='" + serviceId + "'}";
    }
}
