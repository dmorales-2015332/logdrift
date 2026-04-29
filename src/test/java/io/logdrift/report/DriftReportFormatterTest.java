package io.logdrift.report;

import io.logdrift.drift.DriftEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DriftReportFormatterTest {

    private DriftReportFormatter formatter;

    @BeforeEach
    void setUp() {
        formatter = new DriftReportFormatter();
    }

    @Test
    void format_noDrift_containsPassMessage() {
        DriftReport report = new DriftReport("v1.0", "v1.1", List.of());
        String output = formatter.format(report);
        assertThat(output).contains("No schema drift detected");
        assertThat(output).contains("v1.0");
        assertThat(output).contains("v1.1");
    }

    @Test
    void format_withDrift_containsEventDetails() {
        DriftEvent event = new DriftEvent("FIELD_REMOVED", "userId", "Field 'userId' was removed");
        DriftReport report = new DriftReport("v1.0", "v1.1", List.of(event));
        String output = formatter.format(report);
        assertThat(output).contains("1 drift event(s) detected");
        assertThat(output).contains("FIELD_REMOVED");
        assertThat(output).contains("userId");
        assertThat(output).contains("Field 'userId' was removed");
    }

    @Test
    void formatSummaryLine_noDrift_returnsPass() {
        DriftReport report = new DriftReport("v2.0", "v2.1", List.of());
        String summary = formatter.formatSummaryLine(report);
        assertThat(summary).startsWith("PASS");
        assertThat(summary).contains("v2.0").contains("v2.1");
    }

    @Test
    void formatSummaryLine_withDrift_returnsFail() {
        DriftEvent e1 = new DriftEvent("TYPE_CHANGED", "timestamp", "Type changed from string to long");
        DriftEvent e2 = new DriftEvent("FIELD_ADDED", "traceId", "New field 'traceId' added");
        DriftReport report = new DriftReport("v2.0", "v2.1", List.of(e1, e2));
        String summary = formatter.formatSummaryLine(report);
        assertThat(summary).startsWith("FAIL");
        assertThat(summary).contains("2 drift event(s)");
    }
}
