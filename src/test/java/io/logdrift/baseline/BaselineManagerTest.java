package io.logdrift.baseline;

import io.logdrift.schema.LogSchema;
import io.logdrift.schema.LogSchemaExtractor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BaselineManagerTest {

    @Mock
    private BaselineStore store;

    @Mock
    private LogSchemaExtractor extractor;

    private BaselineManager manager;

    @BeforeEach
    void setUp() {
        manager = new BaselineManager(store, extractor);
    }

    @Test
    void captureBaseline_extractsAndSaves() throws IOException {
        List<String> lines = List.of("{\"timestamp\":\"2024-01-01\",\"level\":\"INFO\"}");
        LogSchema schema = new LogSchema("auth-service", Set.of("timestamp", "level"));
        when(extractor.extract(lines)).thenReturn(schema);

        LogSchema result = manager.captureBaseline("auth-service", lines);

        assertThat(result).isEqualTo(schema);
        verify(store).saveBaseline("auth-service", schema);
    }

    @Test
    void captureBaseline_throwsOnEmptyLines() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> manager.captureBaseline("svc", List.of()));
    }

    @Test
    void getBaseline_delegatesToStore() throws IOException {
        LogSchema schema = new LogSchema("inventory", Set.of("field1"));
        when(store.loadBaseline("inventory")).thenReturn(Optional.of(schema));

        Optional<LogSchema> result = manager.getBaseline("inventory");

        assertThat(result).contains(schema);
    }

    @Test
    void hasBaseline_returnsFalse_whenNotPresent() throws IOException {
        when(store.loadBaseline("missing")).thenReturn(Optional.empty());

        assertThat(manager.hasBaseline("missing")).isFalse();
    }

    @Test
    void removeBaseline_delegatesToStore() {
        when(store.deleteBaseline("old-svc")).thenReturn(true);

        assertThat(manager.removeBaseline("old-svc")).isTrue();
    }
}
