package io.logdrift.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Extracts a flat schema (field -> type) from a structured JSON log line.
 * Supports nested objects using dot-notation keys.
 */
public class LogSchemaExtractor {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Parse a raw JSON log line and return its schema as a field-to-type map.
     *
     * @param jsonLine a single JSON log entry
     * @return map of dot-notation field paths to their JSON type names
     * @throws IllegalArgumentException if the input is not valid JSON
     */
    public Map<String, String> extract(String jsonLine) {
        if (jsonLine == null || jsonLine.isBlank()) {
            throw new IllegalArgumentException("Log line must not be null or blank");
        }
        JsonNode root;
        try {
            root = MAPPER.readTree(jsonLine);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JSON log line: " + e.getMessage(), e);
        }
        if (!root.isObject()) {
            throw new IllegalArgumentException("Log line must be a JSON object at the root level");
        }
        Map<String, String> schema = new LinkedHashMap<>();
        collectFields(root, "", schema);
        return schema;
    }

    private void collectFields(JsonNode node, String prefix, Map<String, String> schema) {
        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            JsonNode value = entry.getValue();
            if (value.isObject()) {
                collectFields(value, key, schema);
            } else {
                schema.put(key, resolveType(value));
            }
        }
    }

    private String resolveType(JsonNode node) {
        if (node.isTextual())   return "string";
        if (node.isIntegralNumber()) return "integer";
        if (node.isFloatingPointNumber()) return "float";
        if (node.isBoolean())   return "boolean";
        if (node.isNull())      return "null";
        if (node.isArray())     return "array";
        return "unknown";
    }
}
