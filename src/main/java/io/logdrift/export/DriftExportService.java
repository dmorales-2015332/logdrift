package io.logdrift.export;

import io.logdrift.drift.DriftEvent;
import io.logdrift.report.DriftReport;

import java.io.IOException;
import java.io.Writer;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Exports drift events and reports to various output formats (CSV, JSON lines).
 */
public class DriftExportService {

    public enum ExportFormat {
        CSV, JSONL
    }

    private final ExportFormat format;

    public DriftExportService(ExportFormat format) {
        this.format = Objects.requireNonNull(format, "format must not be null");
    }

    public void export(List<DriftEvent> events, Writer writer) throws IOException {
        Objects.requireNonNull(events, "events must not be null");
        Objects.requireNonNull(writer, "writer must not be null");

        if (format == ExportFormat.CSV) {
            exportCsv(events, writer);
        } else {
            exportJsonl(events, writer);
        }
    }

    public void exportReport(DriftReport report, Writer writer) throws IOException {
        Objects.requireNonNull(report, "report must not be null");
        Objects.requireNonNull(writer, "writer must not be null");
        export(report.getEvents(), writer);
    }

    private void exportCsv(List<DriftEvent> events, Writer writer) throws IOException {
        writer.write("service,field,driftType,detectedAt,severity\n");
        for (DriftEvent event : events) {
            writer.write(String.format("%s,%s,%s,%s,%s\n",
                    escapeCsv(event.getServiceName()),
                    escapeCsv(event.getFieldName()),
                    escapeCsv(event.getDriftType()),
                    event.getDetectedAt() != null ? event.getDetectedAt().toString() : "",
                    escapeCsv(event.getSeverity())));
        }
    }

    private void exportJsonl(List<DriftEvent> events, Writer writer) throws IOException {
        for (DriftEvent event : events) {
            writer.write(String.format(
                    "{\"service\":\"%s\",\"field\":\"%s\",\"driftType\":\"%s\",\"detectedAt\":\"%s\",\"severity\":\"%s\"}\n",
                    escape(event.getServiceName()),
                    escape(event.getFieldName()),
                    escape(event.getDriftType()),
                    event.getDetectedAt() != null ? event.getDetectedAt().toString() : "",
                    escape(event.getSeverity())));
        }
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private String escape(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
