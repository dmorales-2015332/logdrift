package io.logdrift.normalize;

import io.logdrift.drift.DriftEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DriftEventNormalizerTest {

    private NormalizationConfig config;
    private DriftEventNormalizer normalizer;

    @BeforeEach
    void setUp() {
        config = NormalizationConfig.builder()
                .lowercaseServiceNames(true)
                .snakeCaseFields(true)
                .defaultServiceName("unknown-service")
                .build();
        normalizer = new DriftEventNormalizer(config);
    }

    @Test
    void normalize_lowercasesServiceName() {
        DriftEvent event = new DriftEvent("OrderService", "userId", "ADDED", Instant.now(), null, "string");
        DriftEvent result = normalizer.normalize(event);
        assertEquals("orderservice", result.getServiceName());
    }

    @Test
    void normalize_convertsFieldToSnakeCase() {
        DriftEvent event = new DriftEvent("svc", "requestBody", "TYPE_CHANGED", Instant.now(), "int", "string");
        DriftEvent result = normalizer.normalize(event);
        assertEquals("request_body", result.getFieldName());
    }

    @Test
    void normalize_uppercasesDriftType() {
        DriftEvent event = new DriftEvent("svc", "field", "removed", Instant.now(), "string", null);
        DriftEvent result = normalizer.normalize(event);
        assertEquals("REMOVED", result.getDriftType());
    }

    @Test
    void normalize_usesDefaultServiceNameWhenBlank() {
        DriftEvent event = new DriftEvent("  ", "field", "ADDED", Instant.now(), null, "boolean");
        DriftEvent result = normalizer.normalize(event);
        assertEquals("unknown-service", result.getServiceName());
    }

    @Test
    void normalize_setsUnknownFieldWhenFieldNameIsNull() {
        DriftEvent event = new DriftEvent("svc", null, "ADDED", Instant.now(), null, "string");
        DriftEvent result = normalizer.normalize(event);
        assertEquals("unknown_field", result.getFieldName());
    }

    @Test
    void normalizeAll_skipsNullEntries() {
        DriftEvent valid = new DriftEvent("SVC", "myField", "added", Instant.now(), null, "int");
        List<DriftEvent> events = Arrays.asList(valid, null);
        List<DriftEvent> results = normalizer.normalizeAll(events);
        assertEquals(1, results.size());
        assertEquals("svc", results.get(0).getServiceName());
    }

    @Test
    void normalizeAll_throwsOnNullList() {
        assertThrows(NullPointerException.class, () -> normalizer.normalizeAll(null));
    }

    @Test
    void constructor_throwsOnNullConfig() {
        assertThrows(NullPointerException.class, () -> new DriftEventNormalizer(null));
    }
}
