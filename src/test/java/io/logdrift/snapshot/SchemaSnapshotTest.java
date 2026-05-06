package io.logdrift.snapshot;

import io.logdrift.schema.LogSchema;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SchemaSnapshotTest {

    private LogSchema buildSchema() {
        return new LogSchema(Map.of("level", "STRING", "message", "STRING"), Set.of("level"));
    }

    @Test
    void constructor_shouldStoreAllFields() {
        Instant now = Instant.now();
        SchemaSnapshot snapshot = new SchemaSnapshot("svc-auth", "v1.2.0", buildSchema(), now);

        assertEquals("svc-auth", snapshot.getServiceId());
        assertEquals("v1.2.0", snapshot.getVersion());
        assertNotNull(snapshot.getSchema());
        assertEquals(now, snapshot.getCapturedAt());
    }

    @Test
    void constructor_shouldThrowOnNullServiceId() {
        assertThrows(NullPointerException.class,
                () -> new SchemaSnapshot(null, "v1", buildSchema(), Instant.now()));
    }

    @Test
    void constructor_shouldThrowOnNullVersion() {
        assertThrows(NullPointerException.class,
                () -> new SchemaSnapshot("svc", null, buildSchema(), Instant.now()));
    }

    @Test
    void constructor_shouldThrowOnNullSchema() {
        assertThrows(NullPointerException.class,
                () -> new SchemaSnapshot("svc", "v1", null, Instant.now()));
    }

    @Test
    void constructor_shouldThrowOnNullCapturedAt() {
        assertThrows(NullPointerException.class,
                () -> new SchemaSnapshot("svc", "v1", buildSchema(), null));
    }

    @Test
    void equals_shouldBeTrueForSameServiceVersionAndTime() {
        Instant now = Instant.now();
        SchemaSnapshot a = new SchemaSnapshot("svc", "v1", buildSchema(), now);
        SchemaSnapshot b = new SchemaSnapshot("svc", "v1", buildSchema(), now);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void equals_shouldBeFalseForDifferentVersion() {
        Instant now = Instant.now();
        SchemaSnapshot a = new SchemaSnapshot("svc", "v1", buildSchema(), now);
        SchemaSnapshot b = new SchemaSnapshot("svc", "v2", buildSchema(), now);
        assertNotEquals(a, b);
    }

    @Test
    void equals_shouldBeFalseForDifferentServiceId() {
        Instant now = Instant.now();
        SchemaSnapshot a = new SchemaSnapshot("svc-a", "v1", buildSchema(), now);
        SchemaSnapshot b = new SchemaSnapshot("svc-b", "v1", buildSchema(), now);
        assertNotEquals(a, b);
    }

    @Test
    void toString_shouldContainServiceIdAndVersion() {
        SchemaSnapshot snapshot = new SchemaSnapshot("svc-orders", "v3.0", buildSchema(), Instant.now());
        String str = snapshot.toString();
        assertTrue(str.contains("svc-orders"));
        assertTrue(str.contains("v3.0"));
    }
}
