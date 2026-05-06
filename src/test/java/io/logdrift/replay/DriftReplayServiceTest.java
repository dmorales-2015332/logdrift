package io.logdrift.replay;

import io.logdrift.compare.ComparisonResult;
import io.logdrift.compare.SchemaComparator;
import io.logdrift.schema.LogSchema;
import io.logdrift.snapshot.SchemaSnapshot;
import io.logdrift.snapshot.SchemaSnapshotService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DriftReplayServiceTest {

    @Mock private SchemaSnapshotService snapshotService;
    @Mock private SchemaComparator comparator;
    @Mock private LogSchema schemaA;
    @Mock private LogSchema schemaB;
    @Mock private LogSchema schemaC;
    @Mock private ComparisonResult noDrift;
    @Mock private ComparisonResult withDrift;

    private DriftReplayService replayService;

    private static final String SERVICE = "order-service";
    private static final Instant FROM = Instant.parse("2024-01-01T00:00:00Z");
    private static final Instant TO   = Instant.parse("2024-01-02T00:00:00Z");

    @BeforeEach
    void setUp() {
        replayService = new DriftReplayService(snapshotService, comparator);
        when(noDrift.hasDrift()).thenReturn(false);
        when(withDrift.hasDrift()).thenReturn(true);
    }

    @Test
    void replay_returnsOnlyDriftingTransitions() {
        SchemaSnapshot s1 = mockSnapshot(schemaA, Instant.parse("2024-01-01T06:00:00Z"));
        SchemaSnapshot s2 = mockSnapshot(schemaB, Instant.parse("2024-01-01T12:00:00Z"));
        SchemaSnapshot s3 = mockSnapshot(schemaC, Instant.parse("2024-01-01T18:00:00Z"));

        when(snapshotService.getSnapshotsInRange(SERVICE, FROM, TO)).thenReturn(List.of(s1, s2, s3));
        when(comparator.compare(schemaA, schemaB)).thenReturn(noDrift);
        when(comparator.compare(schemaB, schemaC)).thenReturn(withDrift);

        List<DriftReplayResult> results = replayService.replay(SERVICE, FROM, TO);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getServiceId()).isEqualTo(SERVICE);
        assertThat(results.get(0).getPreviousSnapshot()).isSameAs(s2);
        assertThat(results.get(0).getCurrentSnapshot()).isSameAs(s3);
    }

    @Test
    void replay_emptyWhenNoSnapshots() {
        when(snapshotService.getSnapshotsInRange(SERVICE, FROM, TO)).thenReturn(List.of());
        assertThat(replayService.replay(SERVICE, FROM, TO)).isEmpty();
    }

    @Test
    void replay_throwsWhenFromNotBeforeTo() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> replayService.replay(SERVICE, TO, FROM));
    }

    @Test
    void countDriftTransitions_matchesReplaySize() {
        SchemaSnapshot s1 = mockSnapshot(schemaA, Instant.parse("2024-01-01T06:00:00Z"));
        SchemaSnapshot s2 = mockSnapshot(schemaB, Instant.parse("2024-01-01T12:00:00Z"));
        when(snapshotService.getSnapshotsInRange(SERVICE, FROM, TO)).thenReturn(List.of(s1, s2));
        when(comparator.compare(schemaA, schemaB)).thenReturn(withDrift);

        assertThat(replayService.countDriftTransitions(SERVICE, FROM, TO)).isEqualTo(1);
    }

    private SchemaSnapshot mockSnapshot(LogSchema schema, Instant capturedAt) {
        SchemaSnapshot snap = mock(SchemaSnapshot.class);
        when(snap.getSchema()).thenReturn(schema);
        when(snap.getCapturedAt()).thenReturn(capturedAt);
        return snap;
    }
}
