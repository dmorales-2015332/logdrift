package io.logdrift.baseline;

import io.logdrift.schema.LogSchema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

class BaselineStoreTest {

    @TempDir
    Path tempDir;

    private BaselineStore store;

    @BeforeEach
    void setUp() throws IOException {
        store = new BaselineStore(tempDir);
    }

    @Test
    void saveAndLoadBaseline_roundtrip() throws IOException {
        LogSchema schema = new LogSchema("order-service", Set.of("timestamp", "level", "message"));
        store.saveBaseline("order-service", schema);

        Optional<LogSchema> loaded = store.loadBaseline("order-service");

        assertThat(loaded).isPresent();
        assertThat(loaded.get().getServiceName()).isEqualTo("order-service");
        assertThat(loaded.get().getFields()).containsExactlyInAnyOrder("timestamp", "level", "message");
    }

    @Test
    void loadBaseline_returnsEmpty_whenNotFound() throws IOException {
        Optional<LogSchema> result = store.loadBaseline("nonexistent-service");
        assertThat(result).isEmpty();
    }

    @Test
    void deleteBaseline_removesFile() throws IOException {
        LogSchema schema = new LogSchema("payment-service", Set.of("timestamp", "traceId"));
        store.saveBaseline("payment-service", schema);

        boolean deleted = store.deleteBaseline("payment-service");

        assertThat(deleted).isTrue();
        assertThat(store.loadBaseline("payment-service")).isEmpty();
    }

    @Test
    void loadAllBaselines_returnsAllSaved() throws IOException {
        store.saveBaseline("svc-a", new LogSchema("svc-a", Set.of("a", "b")));
        store.saveBaseline("svc-b", new LogSchema("svc-b", Set.of("c", "d")));

        Map<String, LogSchema> all = store.loadAllBaselines();

        assertThat(all).containsKeys("svc-a", "svc-b");
    }

    @Test
    void saveBaseline_throwsOnBlankServiceName() {
        LogSchema schema = new LogSchema("x", Set.of("field"));
        assertThatIllegalArgumentException()
            .isThrownBy(() -> store.saveBaseline("", schema));
    }
}
