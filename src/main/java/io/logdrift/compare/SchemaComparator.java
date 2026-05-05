package io.logdrift.compare;

import io.logdrift.schema.LogSchema;
import io.logdrift.drift.DriftEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

/**
 * Compares two LogSchema instances and produces a list of DriftEvents
 * describing structural differences between them.
 */
public class SchemaComparator {

    /**
     * Compares a candidate schema against a baseline schema.
     *
     * @param baseline  the reference schema
     * @param candidate the schema to evaluate
     * @param service   the service name for event attribution
     * @return list of drift events representing schema differences
     */
    public List<DriftEvent> compare(LogSchema baseline, LogSchema candidate, String service) {
        if (baseline == null || candidate == null) {
            throw new IllegalArgumentException("Neither baseline nor candidate schema may be null");
        }

        List<DriftEvent> events = new ArrayList<>();

        Map<String, String> baselineFields = baseline.getFields();
        Map<String, String> candidateFields = candidate.getFields();

        Set<String> allKeys = new HashSet<>();
        allKeys.addAll(baselineFields.keySet());
        allKeys.addAll(candidateFields.keySet());

        for (String key : allKeys) {
            boolean inBaseline  = baselineFields.containsKey(key);
            boolean inCandidate = candidateFields.containsKey(key);

            if (inBaseline && !inCandidate) {
                events.add(DriftEvent.fieldRemoved(service, key, baselineFields.get(key)));
            } else if (!inBaseline && inCandidate) {
                events.add(DriftEvent.fieldAdded(service, key, candidateFields.get(key)));
            } else {
                String baselineType  = baselineFields.get(key);
                String candidateType = candidateFields.get(key);
                if (!baselineType.equals(candidateType)) {
                    events.add(DriftEvent.typeChanged(service, key, baselineType, candidateType));
                }
            }
        }

        return events;
    }
}
