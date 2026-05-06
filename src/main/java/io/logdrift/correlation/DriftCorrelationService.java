package io.logdrift.correlation;

import io.logdrift.drift.DriftEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Groups drift events into correlation groups based on service, field, and drift type.
 * Useful for identifying recurring or cross-service schema drift patterns.
 */
public class DriftCorrelationService {

    private final Map<DriftCorrelationKey, DriftCorrelationGroup> groups = new LinkedHashMap<>();

    /**
     * Ingests a list of drift events and builds correlation groups.
     *
     * @param events the drift events to correlate
     */
    public void correlate(List<DriftEvent> events) {
        Objects.requireNonNull(events, "events must not be null");
        for (DriftEvent event : events) {
            DriftCorrelationKey key = new DriftCorrelationKey(
                    event.getServiceId(),
                    event.getFieldName(),
                    event.getDriftType()
            );
            groups.computeIfAbsent(key, DriftCorrelationGroup::new).addEvent(event);
        }
    }

    /**
     * Returns all correlation groups discovered so far.
     */
    public List<DriftCorrelationGroup> getGroups() {
        return Collections.unmodifiableList(new ArrayList<>(groups.values()));
    }

    /**
     * Returns only groups that contain more than one event (recurring drift).
     */
    public List<DriftCorrelationGroup> getRecurringGroups() {
        List<DriftCorrelationGroup> recurring = new ArrayList<>();
        for (DriftCorrelationGroup group : groups.values()) {
            if (group.isRecurring()) {
                recurring.add(group);
            }
        }
        return Collections.unmodifiableList(recurring);
    }

    /**
     * Returns groups that span more than one service for the same field and drift type.
     */
    public List<DriftCorrelationGroup> getCrossServiceGroups() {
        Map<String, List<DriftCorrelationGroup>> byFieldAndType = new LinkedHashMap<>();
        for (DriftCorrelationGroup group : groups.values()) {
            String compositeKey = group.getKey().getFieldName() + ":" + group.getKey().getDriftType();
            byFieldAndType.computeIfAbsent(compositeKey, k -> new ArrayList<>()).add(group);
        }
        List<DriftCorrelationGroup> crossService = new ArrayList<>();
        for (List<DriftCorrelationGroup> candidates : byFieldAndType.values()) {
            if (candidates.size() > 1) {
                crossService.addAll(candidates);
            }
        }
        return Collections.unmodifiableList(crossService);
    }

    /**
     * Clears all accumulated correlation state.
     */
    public void reset() {
        groups.clear();
    }
}
