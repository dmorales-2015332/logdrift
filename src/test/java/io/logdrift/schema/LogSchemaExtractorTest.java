package io.logdrift.schema;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LogSchemaExtractorTest {

    private LogSchemaExtractor extractor;

    @BeforeEach
    void setUp() {
        extractor = new LogSchemaExtractor();
    }

    @Test
    void extractsFlatSchema() {
        String line = "{\"level\":\"INFO\",\"message\":\"started\",\"pid\":42,\"success\":true}";
        Map<String, String> schema = extractor.extract(line);
        assertEquals("string",  schema.get("level"));
        assertEquals("string",  schema.get("message"));
        assertEquals("integer", schema.get("pid"));
        assertEquals("boolean", schema.get("success"));
    }

    @Test
    void extractsNestedSchemaWithDotNotation() {
        String line = "{\"http\":{\"method\":\"GET\",\"status\":200},\"duration\":0.45}";
        Map<String, String> schema = extractor.extract(line);
        assertEquals("string",  schema.get("http.method"));
        assertEquals("integer", schema.get("http.status"));
        assertEquals("float",   schema.get("duration"));
        assertFalse(schema.containsKey("http"), "Intermediate object nodes should not appear as keys");
    }

    @Test
    void handlesNullValues() {
        String line = "{\"traceId\":null}";
        Map<String, String> schema = extractor.extract(line);
        assertEquals("null", schema.get("traceId"));
    }

    @Test
    void handlesArrayValues() {
        String line = "{\"tags\":[\"a\",\"b\"]}";
        Map<String, String> schema = extractor.extract(line);
        assertEquals("array", schema.get("tags"));
    }

    @Test
    void throwsOnBlankInput() {
        assertThrows(IllegalArgumentException.class, () -> extractor.extract("   "));
    }

    @Test
    void throwsOnInvalidJson() {
        assertThrows(IllegalArgumentException.class, () -> extractor.extract("not json"));
    }

    @Test
    void throwsOnNonObjectRoot() {
        assertThrows(IllegalArgumentException.class, () -> extractor.extract("[1,2,3]"));
    }
}
