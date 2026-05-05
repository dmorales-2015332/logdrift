package io.logdrift.alert;

import io.logdrift.drift.DriftEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class DriftAlertPolicyTest {

    private DriftEvent eventWith(List<String> missing, List<String> added, Map<String, String> typeChanges) {
        DriftEvent event = mock(DriftEvent.class);
        when(event.getMissingFields()).thenReturn(missing);
        when(event.getAddedFields()).thenReturn(added);
        when(event.getTypeChanges()).thenReturn(typeChanges);
        return event;
    }

    @Test
    void shouldNotAlertWhenWithinAllThresholds() {
        DriftAlertPolicy policy = DriftAlertPolicy.builder()
                .maxMissingFieldsAllowed(2)
                .maxAddedFieldsAllowed(3)
                .maxTypeChangesAllowed(1)
                .build();

        DriftEvent event = eventWith(List.of("fieldA"), List.of("x", "y"), Map.of("ts", "string->long"));
        assertThat(policy.shouldAlert(event)).isFalse();
    }

    @Test
    void shouldAlertWhenMissingFieldsExceedThreshold() {
        DriftAlertPolicy policy = DriftAlertPolicy.builder().maxMissingFieldsAllowed(1).build();
        DriftEvent event = eventWith(List.of("a", "b"), List.of(), Map.of());
        assertThat(policy.shouldAlert(event)).isTrue();
    }

    @Test
    void shouldAlertWhenAddedFieldsExceedThreshold() {
        DriftAlertPolicy policy = DriftAlertPolicy.builder().maxAddedFieldsAllowed(1).build();
        DriftEvent event = eventWith(List.of(), List.of("x", "y"), Map.of());
        assertThat(policy.shouldAlert(event)).isTrue();
    }

    @Test
    void shouldAlertWhenTypeChangesExceedThreshold() {
        DriftAlertPolicy policy = DriftAlertPolicy.builder().maxTypeChangesAllowed(0).build();
        DriftEvent event = eventWith(List.of(), List.of(), Map.of("level", "int->string"));
        assertThat(policy.shouldAlert(event)).isTrue();
    }

    @Test
    void shouldAlertWhenCriticalFieldIsMissing() {
        DriftAlertPolicy policy = DriftAlertPolicy.builder()
                .maxMissingFieldsAllowed(5)
                .criticalField("traceId")
                .build();

        DriftEvent event = eventWith(List.of("traceId"), List.of(), Map.of());
        assertThat(policy.shouldAlert(event)).isTrue();
    }

    @Test
    void shouldAlertWhenCriticalFieldHasTypeChange() {
        DriftAlertPolicy policy = DriftAlertPolicy.builder()
                .maxTypeChangesAllowed(5)
                .criticalField("userId")
                .build();

        DriftEvent event = eventWith(List.of(), List.of(), Map.of("userId", "int->string"));
        assertThat(policy.shouldAlert(event)).isTrue();
    }

    @Test
    void shouldThrowOnNullEvent() {
        DriftAlertPolicy policy = DriftAlertPolicy.builder().build();
        assertThatThrownBy(() -> policy.shouldAlert(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("DriftEvent must not be null");
    }

    @Test
    void defaultPolicyRejectsAnyMissingOrTypeChange() {
        DriftAlertPolicy policy = DriftAlertPolicy.builder().build();
        assertThat(policy.getMaxMissingFieldsAllowed()).isZero();
        assertThat(policy.getMaxTypeChangesAllowed()).isZero();
        assertThat(policy.getMaxAddedFieldsAllowed()).isEqualTo(5);
    }
}
