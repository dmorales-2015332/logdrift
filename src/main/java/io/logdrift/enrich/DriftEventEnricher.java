package io.logdrift.enrich;

import io.logdrift.drift.DriftEvent;
import io.logdrift.tag.DriftTagService;
import io.logdrift.correlation.DriftCorrelationKey;
import io.logdrift.classify.DriftClassification;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Enriches DriftEvents with additional metadata such as environment context,
 * resolved tags, correlation keys, and enrichment timestamps.
 */
public class DriftEventEnricher {

    private final DriftTagService tagService;
    private final Map<String, String> staticContext;

    public DriftEventEnricher(DriftTagService tagService, Map<String, String> staticContext) {
        this.tagService = Objects.requireNonNull(tagService, "tagService must not be null");
        this.staticContext = staticContext != null ? Map.copyOf(staticContext) : Map.of();
    }

    /**
     * Enriches a DriftEvent by producing an EnrichedDriftEvent with resolved metadata.
     *
     * @param event the raw drift event to enrich
     * @return an enriched representation of the event
     */
    public EnrichedDriftEvent enrich(DriftEvent event) {
        Objects.requireNonNull(event, "event must not be null");

        Map<String, String> metadata = new HashMap<>(staticContext);
        metadata.put("enrichedAt", Instant.now().toString());
        metadata.put("service", event.getServiceName());
        metadata.put("fieldName", event.getFieldName());

        var tags = tagService.getTagsForService(event.getServiceName());
        tags.forEach(tag -> metadata.put("tag." + tag.getKey(), tag.getValue()));

        var correlationKey = DriftCorrelationKey.of(event.getServiceName(), event.getFieldName());

        return new EnrichedDriftEvent(event, metadata, correlationKey);
    }

    /**
     * Enriches multiple events in batch.
     *
     * @param events list of raw drift events
     * @return list of enriched drift events
     */
    public java.util.List<EnrichedDriftEvent> enrichAll(java.util.List<DriftEvent> events) {
        Objects.requireNonNull(events, "events must not be null");
        return events.stream().map(this::enrich).toList();
    }
}
