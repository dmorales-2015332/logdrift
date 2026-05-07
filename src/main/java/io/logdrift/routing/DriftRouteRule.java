package io.logdrift.routing;

import java.util.Objects;

/**
 * Represents a routing rule that maps a service name pattern to a named destination.
 */
public class DriftRouteRule {

    private final String servicePattern;
    private final String destination;
    private final int priority;

    public DriftRouteRule(String servicePattern, String destination, int priority) {
        if (servicePattern == null || servicePattern.isBlank()) {
            throw new IllegalArgumentException("Service pattern must not be blank");
        }
        if (destination == null || destination.isBlank()) {
            throw new IllegalArgumentException("Destination must not be blank");
        }
        this.servicePattern = servicePattern;
        this.destination = destination;
        this.priority = priority;
    }

    public boolean matches(String serviceName) {
        if (serviceName == null) return false;
        if (servicePattern.equals("*")) return true;
        if (servicePattern.endsWith("*")) {
            String prefix = servicePattern.substring(0, servicePattern.length() - 1);
            return serviceName.startsWith(prefix);
        }
        return servicePattern.equals(serviceName);
    }

    public String getServicePattern() {
        return servicePattern;
    }

    public String getDestination() {
        return destination;
    }

    public int getPriority() {
        return priority;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DriftRouteRule)) return false;
        DriftRouteRule that = (DriftRouteRule) o;
        return priority == that.priority &&
               Objects.equals(servicePattern, that.servicePattern) &&
               Objects.equals(destination, that.destination);
    }

    @Override
    public int hashCode() {
        return Objects.hash(servicePattern, destination, priority);
    }

    @Override
    public String toString() {
        return "DriftRouteRule{pattern='" + servicePattern + "', destination='" + destination + "', priority=" + priority + "}";
    }
}
