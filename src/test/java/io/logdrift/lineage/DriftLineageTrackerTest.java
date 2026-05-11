package io.logdrift.lineage;

import io.logdrift.drift.DriftEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

class DriftLineageTrackerTest {

    private DriftLineageTracker tracker;

    @BeforeEach
    void setUp() {
        tracker = new DriftLineageTracker();
    }

    @Test
    void record_singleEvent_storesOneEntry() {
        DriftEvent event = DriftEvent.builder()
                .serviceName("order-service")
                .fieldName("orderId")
                .driftType("TYPE_CHANGED")
                .timestamp(Instant.parse("2024-06-01T10:00:00Z"))
                .build();

        tracker.record(event);

        List<DriftLineageEntry> lineage = tracker.getLineage("order-service", "orderId");
        assertThat(lineage).hasSize(1);
        assertThat(lineage.get(0).fieldName()).isEqualTo("orderId");
        assertThat(lineage.get(0).driftType()).isEqualTo("TYPE_CHANGED");
    }

    @Test
    void record_multipleEventsForSameField_preservesOrder() {
        Instant t1 = Instant.parse("2024-06-01T10:00:00Z");
        Instant t2 = Instant.parse("2024-06-02T10:00:00Z");

        tracker.record(buildEvent("svc", "price", "FIELD_ADDED", t1));
        tracker.record(buildEvent("svc", "price", "TYPE_CHANGED", t2));

        List<DriftLineageEntry> lineage = tracker.getLineage("svc", "price");
        assertThat(lineage).hasSize(2);
        assertThat(lineage.get(0).driftType()).isEqualTo("FIELD_ADDED");
        assertThat(lineage.get(1).driftType()).isEqualTo("TYPE_CHANGED");
    }

    @Test
    void getLineage_unknownField_returnsEmptyList() {
        List<DriftLineageEntry> lineage = tracker.getLineage("unknown-svc", "ghost");
        assertThat(lineage).isEmpty();
    }

    @Test
    void trackedFields_returnsOnlyFieldsForGivenService() {
        tracker.record(buildEvent("alpha", "fieldA", "FIELD_ADDED", Instant.now()));
        tracker.record(buildEvent("alpha", "fieldB", "FIELD_REMOVED", Instant.now()));
        tracker.record(buildEvent("beta", "fieldA", "TYPE_CHANGED", Instant.now()));

        Set<String> alphaFields = tracker.trackedFields("alpha");
        assertThat(alphaFields).containsExactlyInAnyOrder("fieldA", "fieldB");

        Set<String> betaFields = tracker.trackedFields("beta");
        assertThat(betaFields).containsExactly("fieldA");
    }

    @Test
    void clear_removesAllEntries() {
        tracker.record(buildEvent("svc", "f", "FIELD_ADDED", Instant.now()));
        tracker.clear();

        assertThat(tracker.getLineage("svc", "f")).isEmpty();
        assertThat(tracker.trackedFields("svc")).isEmpty();
    }

    @Test
    void record_nullEvent_throwsNullPointerException() {
        assertThatNullPointerException().isThrownBy(() -> tracker.record(null));
    }

    private DriftEvent buildEvent(String service, String field, String type, Instant ts) {
        return DriftEvent.builder()
                .serviceName(service)
                .fieldName(field)
                .driftType(type)
                .timestamp(ts)
                .build();
    }
}
