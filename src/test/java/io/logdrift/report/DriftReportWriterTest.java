package io.logdrift.report;

import io.logdrift.drift.DriftEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DriftReportWriterTest {

    private DriftReportWriter writer;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        writer = new DriftReportWriter();
    }

    @Test
    void writeToStream_outputsFormattedReport() {
        DriftReport report = new DriftReport("v1", "v2", List.of());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writer.writeToStream(report, new PrintStream(baos));
        String output = baos.toString();
        assertThat(output).contains("LogDrift Report");
        assertThat(output).contains("No schema drift detected");
    }

    @Test
    void writeToFile_createsFileWithContent() throws IOException {
        DriftReport report = new DriftReport("v1", "v2", List.of());
        Path outFile = tempDir.resolve("report.txt");
        writer.writeToFile(report, outFile);
        assertThat(outFile).exists();
        assertThat(Files.readString(outFile)).contains("LogDrift Report");
    }

    @Test
    void writeJsonToString_returnsValidJson() throws IOException {
        DriftEvent event = new DriftEvent("FIELD_REMOVED", "requestId", "Field removed");
        DriftReport report = new DriftReport("v1", "v2", List.of(event));
        String json = writer.writeJsonToString(report);
        assertThat(json).contains("baselineVersion");
        assertThat(json).contains("v1");
        assertThat(json).contains("requestId");
    }

    @Test
    void exitCode_noDrift_returnsZero() {
        DriftReport report = new DriftReport("v1", "v2", List.of());
        assertThat(writer.exitCode(report)).isEqualTo(0);
    }

    @Test
    void exitCode_withDrift_returnsOne() {
        DriftEvent event = new DriftEvent("TYPE_CHANGED", "level", "Type changed");
        DriftReport report = new DriftReport("v1", "v2", List.of(event));
        assertThat(writer.exitCode(report)).isEqualTo(1);
    }
}
