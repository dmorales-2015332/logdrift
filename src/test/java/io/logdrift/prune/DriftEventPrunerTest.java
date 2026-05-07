package io.logdrift.prune;

import io.logdrift.classify.DriftClassification;
import io.logdrift.drift.DriftEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DriftEventPrunerTest {

    private PrunePolicy policy;
    private DriftEventPruner pruner;

    @BeforeEach
    void setUp() {
        policy = mock(PrunePolicy.class);
        when(policy.getMaxAgeSeconds()).thenReturn(0L);
        when(policy.getExcludedServices()).thenReturn(Set.of());
        when(policy.getMinSeverity()).thenReturn(null);
        pruner = new DriftEventPruner(policy);
    }

    @Test
    void nullPolicyShouldThrow() {
        assertThatThrownBy(() -> new DriftEventPruner(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void emptyListRetainsNothing() {
        assertThat(pruner.prune(List.of())).isEmpty();
    }

    @Test
    void nullListReturnsEmpty() {
        assertThat(pruner.prune(null)).isEmpty();
    }

    @Test
    void eventsWithinMaxAgeAreRetained() {
        when(policy.getMaxAgeSeconds()).thenReturn(3600L);
        DriftEvent recent = eventAt(Instant.now().minusSeconds(60), "svc-a", DriftClassification.Severity.HIGH);
        DriftEvent old = eventAt(Instant.now().minusSeconds(7200), "svc-a", DriftClassification.Severity.HIGH);

        List<DriftEvent> result = pruner.prune(List.of(recent, old));

        assertThat(result).containsExactly(recent);
    }

    @Test
    void excludedServicesArePruned() {
        when(policy.getExcludedServices()).thenReturn(Set.of("svc-excluded"));
        DriftEvent kept = eventAt(Instant.now(), "svc-keep", DriftClassification.Severity.LOW);
        DriftEvent excluded = eventAt(Instant.now(), "svc-excluded", DriftClassification.Severity.LOW);

        List<DriftEvent> result = pruner.prune(List.of(kept, excluded));

        assertThat(result).containsExactly(kept);
    }

    @Test
    void eventsBelowMinSeverityArePruned() {
        when(policy.getMinSeverity()).thenReturn(DriftClassification.Severity.HIGH);
        DriftEvent high = eventAt(Instant.now(), "svc", DriftClassification.Severity.HIGH);
        DriftEvent low = eventAt(Instant.now(), "svc", DriftClassification.Severity.LOW);

        List<DriftEvent> result = pruner.prune(List.of(high, low));

        assertThat(result).containsExactly(high);
    }

    @Test
    void countPrunableMatchesPrunedCount() {
        when(policy.getMaxAgeSeconds()).thenReturn(3600L);
        DriftEvent recent = eventAt(Instant.now().minusSeconds(30), "svc", DriftClassification.Severity.LOW);
        DriftEvent old = eventAt(Instant.now().minusSeconds(9000), "svc", DriftClassification.Severity.LOW);

        assertThat(pruner.countPrunable(List.of(recent, old))).isEqualTo(1);
    }

    private DriftEvent eventAt(Instant time, String service, DriftClassification.Severity severity) {
        DriftEvent event = mock(DriftEvent.class);
        when(event.getDetectedAt()).thenReturn(time);
        when(event.getServiceName()).thenReturn(service);
        when(event.getSeverity()).thenReturn(severity);
        return event;
    }
}
