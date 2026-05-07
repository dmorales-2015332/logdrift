package io.logdrift.enrich;

import io.logdrift.drift.DriftEvent;
import io.logdrift.tag.DriftTag;
import io.logdrift.tag.DriftTagService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class DriftEventEnricherTest {

    private DriftTagService tagService;
    private DriftEventEnricher enricher;

    @BeforeEach
    void setUp() {
        tagService = Mockito.mock(DriftTagService.class);
        enricher = new DriftEventEnricher(tagService, Map.of("env", "test"));
    }

    @Test
    void enrich_shouldPopulateMetadataFromStaticContext() {
        var event = DriftEvent.of("order-service", "orderId", "MISSING_FIELD");
        when(tagService.getTagsForService("order-service")).thenReturn(List.of());

        var result = enricher.enrich(event);

        assertThat(result).isNotNull();
        assertThat(result.getMetadata()).containsEntry("env", "test");
        assertThat(result.getMetadata()).containsKey("enrichedAt");
    }

    @Test
    void enrich_shouldIncludeServiceAndFieldInMetadata() {
        var event = DriftEvent.of("payment-service", "amount", "TYPE_CHANGE");
        when(tagService.getTagsForService("payment-service")).thenReturn(List.of());

        var result = enricher.enrich(event);

        assertThat(result.getMetadata()).containsEntry("service", "payment-service");
        assertThat(result.getMetadata()).containsEntry("fieldName", "amount");
    }

    @Test
    void enrich_shouldResolveTags() {
        var event = DriftEvent.of("inventory-service", "sku", "ADDED_FIELD");
        var tag = DriftTag.of("team", "platform");
        when(tagService.getTagsForService("inventory-service")).thenReturn(List.of(tag));

        var result = enricher.enrich(event);

        assertThat(result.getMetadata()).containsEntry("tag.team", "platform");
    }

    @Test
    void enrich_shouldSetCorrelationKey() {
        var event = DriftEvent.of("user-service", "email", "MISSING_FIELD");
        when(tagService.getTagsForService("user-service")).thenReturn(List.of());

        var result = enricher.enrich(event);

        assertThat(result.getCorrelationKey()).isNotNull();
        assertThat(result.getCorrelationKey().getService()).isEqualTo("user-service");
        assertThat(result.getCorrelationKey().getField()).isEqualTo("email");
    }

    @Test
    void enrichAll_shouldEnrichEveryEvent() {
        var e1 = DriftEvent.of("svc-a", "f1", "MISSING_FIELD");
        var e2 = DriftEvent.of("svc-b", "f2", "TYPE_CHANGE");
        when(tagService.getTagsForService(anyString())).thenReturn(List.of());

        var results = enricher.enrichAll(List.of(e1, e2));

        assertThat(results).hasSize(2);
        assertThat(results.get(0).getMetadata()).containsEntry("service", "svc-a");
        assertThat(results.get(1).getMetadata()).containsEntry("service", "svc-b");
    }

    @Test
    void enrich_shouldThrowOnNullEvent() {
        assertThatNullPointerException().isThrownBy(() -> enricher.enrich(null));
    }
}
