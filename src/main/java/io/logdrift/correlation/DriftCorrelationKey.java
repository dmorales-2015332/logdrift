package io.logdrift.correlation;

import java.util.Objects;

/**
 * Identifies a unique correlation key used to group related drift events
 * across services and time windows.
 */
public class DriftCorrelationKey {

    private final String serviceId;
    private final String fieldName;
    private final String driftType;

    public DriftCorrelationKey(String serviceId, String fieldName, String driftType) {
        this.serviceId = Objects.requireNonNull(serviceId, "serviceId must not be null");
        this.fieldName = Objects.requireNonNull(fieldName, "fieldName must not be null");
        this.driftType = Objects.requireNonNull(driftType, "driftType must not be null");
    }

    public String getServiceId() {
        return serviceId;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getDriftType() {
        return driftType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DriftCorrelationKey)) return false;
        DriftCorrelationKey that = (DriftCorrelationKey) o;
        return Objects.equals(serviceId, that.serviceId)
                && Objects.equals(fieldName, that.fieldName)
                && Objects.equals(driftType, that.driftType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceId, fieldName, driftType);
    }

    @Override
    public String toString() {
        return "DriftCorrelationKey{serviceId='" + serviceId + "', fieldName='" + fieldName
                + "', driftType='" + driftType + "'}";
    }
}
