package io.logdrift.tag;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a user-defined tag applied to a drift event or schema snapshot
 * to aid in categorization, filtering, and reporting.
 */
public class DriftTag {

    private final String key;
    private final String value;
    private final String appliedBy;
    private final Instant appliedAt;

    public DriftTag(String key, String value, String appliedBy, Instant appliedAt) {
        if (key == null || key.isBlank()) throw new IllegalArgumentException("Tag key must not be blank");
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Tag value must not be blank");
        this.key = key;
        this.value = value;
        this.appliedBy = appliedBy != null ? appliedBy : "system";
        this.appliedAt = appliedAt != null ? appliedAt : Instant.now();
    }

    public DriftTag(String key, String value) {
        this(key, value, "system", Instant.now());
    }

    public String getKey() { return key; }
    public String getValue() { return value; }
    public String getAppliedBy() { return appliedBy; }
    public Instant getAppliedAt() { return appliedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DriftTag)) return false;
        DriftTag that = (DriftTag) o;
        return Objects.equals(key, that.key) && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }

    @Override
    public String toString() {
        return key + "=" + value;
    }
}
