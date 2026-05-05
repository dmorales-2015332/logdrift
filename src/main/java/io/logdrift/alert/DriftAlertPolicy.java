package io.logdrift.alert;

import io.logdrift.drift.DriftEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Defines thresholds and rules that determine when drift events
 * should trigger alerts.
 */
public class DriftAlertPolicy {

    private final int maxMissingFieldsAllowed;
    private final int maxAddedFieldsAllowed;
    private final int maxTypeChangesAllowed;
    private final List<String> criticalFields;

    private DriftAlertPolicy(Builder builder) {
        this.maxMissingFieldsAllowed = builder.maxMissingFieldsAllowed;
        this.maxAddedFieldsAllowed = builder.maxAddedFieldsAllowed;
        this.maxTypeChangesAllowed = builder.maxTypeChangesAllowed;
        this.criticalFields = List.copyOf(builder.criticalFields);
    }

    public boolean shouldAlert(DriftEvent event) {
        Objects.requireNonNull(event, "DriftEvent must not be null");

        if (event.getMissingFields().size() > maxMissingFieldsAllowed) {
            return true;
        }
        if (event.getAddedFields().size() > maxAddedFieldsAllowed) {
            return true;
        }
        if (event.getTypeChanges().size() > maxTypeChangesAllowed) {
            return true;
        }
        for (String critical : criticalFields) {
            if (event.getMissingFields().contains(critical) ||
                event.getTypeChanges().containsKey(critical)) {
                return true;
            }
        }
        return false;
    }

    public int getMaxMissingFieldsAllowed() { return maxMissingFieldsAllowed; }
    public int getMaxAddedFieldsAllowed()   { return maxAddedFieldsAllowed; }
    public int getMaxTypeChangesAllowed()   { return maxTypeChangesAllowed; }
    public List<String> getCriticalFields() { return criticalFields; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private int maxMissingFieldsAllowed = 0;
        private int maxAddedFieldsAllowed   = 5;
        private int maxTypeChangesAllowed   = 0;
        private final List<String> criticalFields = new ArrayList<>();

        public Builder maxMissingFieldsAllowed(int v) { this.maxMissingFieldsAllowed = v; return this; }
        public Builder maxAddedFieldsAllowed(int v)   { this.maxAddedFieldsAllowed = v;   return this; }
        public Builder maxTypeChangesAllowed(int v)   { this.maxTypeChangesAllowed = v;   return this; }
        public Builder criticalField(String field)    { this.criticalFields.add(field);   return this; }
        public DriftAlertPolicy build()               { return new DriftAlertPolicy(this); }
    }
}
