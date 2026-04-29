package io.logdrift.drift;

import java.util.Objects;

/**
 * Represents a single schema drift event between two log schemas.
 */
public class DriftEvent {

    public enum Type {
        FIELD_ADDED,
        FIELD_REMOVED,
        TYPE_CHANGED
    }

    private final Type type;
    private final String fieldName;
    private final String baselineType;
    private final String candidateType;

    public DriftEvent(Type type, String fieldName, String baselineType, String candidateType) {
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.fieldName = Objects.requireNonNull(fieldName, "fieldName must not be null");
        this.baselineType = baselineType;
        this.candidateType = candidateType;
    }

    public Type getType() { return type; }
    public String getFieldName() { return fieldName; }
    public String getBaselineType() { return baselineType; }
    public String getCandidateType() { return candidateType; }

    @Override
    public String toString() {
        return switch (type) {
            case FIELD_ADDED   -> String.format("[ADDED]   '%s' (%s)", fieldName, candidateType);
            case FIELD_REMOVED -> String.format("[REMOVED] '%s' (%s)", fieldName, baselineType);
            case TYPE_CHANGED  -> String.format("[TYPE]    '%s' %s -> %s", fieldName, baselineType, candidateType);
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DriftEvent that)) return false;
        return type == that.type
                && fieldName.equals(that.fieldName)
                && Objects.equals(baselineType, that.baselineType)
                && Objects.equals(candidateType, that.candidateType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, fieldName, baselineType, candidateType);
    }
}
