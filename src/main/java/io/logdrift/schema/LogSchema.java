package io.logdrift.schema;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents the inferred schema of a structured log source.
 * Fields are stored as a mapping of field-name -> inferred type string.
 */
public class LogSchema {

    private final String sourceName;
    private final Map<String, String> fields;

    public LogSchema(String sourceName, Map<String, String> fields) {
        this.sourceName = Objects.requireNonNull(sourceName, "sourceName must not be null");
        this.fields = Collections.unmodifiableMap(new LinkedHashMap<>(fields));
    }

    /** Logical name of the log source (e.g. service name or file path). */
    public String getSourceName() { return sourceName; }

    /** Returns an unmodifiable view of the field-name -> type mapping. */
    public Map<String, String> getFields() { return fields; }

    /** Returns the number of fields in this schema. */
    public int fieldCount() { return fields.size(); }

    /** Returns {@code true} if the schema contains no fields. */
    public boolean isEmpty() { return fields.isEmpty(); }

    @Override
    public String toString() {
        return "LogSchema{source='" + sourceName + "', fields=" + fields + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LogSchema that)) return false;
        return sourceName.equals(that.sourceName) && fields.equals(that.fields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceName, fields);
    }
}
