package io.logdrift.drift;

import io.logdrift.schema.LogSchema;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Detects schema drift between a baseline log schema and a candidate log schema.
 */
public class SchemaDriftDetector {

    /**
     * Compare two schemas and return a list of drift events.
     *
     * @param baseline the reference schema (e.g. from a previous deployment)
     * @param candidate the schema to validate (e.g. from the current deployment)
     * @return list of detected {@link DriftEvent}s; empty if schemas match
     */
    public List<DriftEvent> detect(LogSchema baseline, LogSchema candidate) {
        if (baseline == null) throw new IllegalArgumentException("baseline schema must not be null");
        if (candidate == null) throw new IllegalArgumentException("candidate schema must not be null");

        List<DriftEvent> events = new ArrayList<>();

        Map<String, String> baseFields = baseline.getFields();
        Map<String, String> candFields = candidate.getFields();

        // Detect missing fields (present in baseline, absent in candidate)
        for (String key : baseFields.keySet()) {
            if (!candFields.containsKey(key)) {
                events.add(new DriftEvent(DriftEvent.Type.FIELD_REMOVED, key,
                        baseFields.get(key), null));
            }
        }

        // Detect added fields and type changes
        for (Map.Entry<String, String> entry : candFields.entrySet()) {
            String key = entry.getKey();
            String candType = entry.getValue();
            if (!baseFields.containsKey(key)) {
                events.add(new DriftEvent(DriftEvent.Type.FIELD_ADDED, key,
                        null, candType));
            } else {
                String baseType = baseFields.get(key);
                if (!baseType.equals(candType)) {
                    events.add(new DriftEvent(DriftEvent.Type.TYPE_CHANGED, key,
                            baseType, candType));
                }
            }
        }

        return events;
    }
}
