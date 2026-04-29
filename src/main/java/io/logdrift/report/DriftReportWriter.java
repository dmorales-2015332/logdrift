package io.logdrift.report;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Writes a {@link DriftReport} to various output targets (stdout, file, JSON).
 */
public class DriftReportWriter {

    private final DriftReportFormatter formatter;
    private final ObjectMapper objectMapper;

    public DriftReportWriter() {
        this.formatter = new DriftReportFormatter();
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .enable(SerializationFeature.INDENT_OUTPUT)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public void writeToStream(DriftReport report, PrintStream out) {
        out.print(formatter.format(report));
    }

    public void writeToFile(DriftReport report, Path outputPath) throws IOException {
        String content = formatter.format(report);
        Files.writeString(outputPath, content);
    }

    public void writeJsonToFile(DriftReport report, Path outputPath) throws IOException {
        objectMapper.writeValue(outputPath.toFile(), report);
    }

    public String writeJsonToString(DriftReport report) throws IOException {
        return objectMapper.writeValueAsString(report);
    }

    public int exitCode(DriftReport report) {
        return report.hasDrift() ? 1 : 0;
    }
}
