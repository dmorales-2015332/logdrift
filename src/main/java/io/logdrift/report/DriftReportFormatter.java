package io.logdrift.report;

import io.logdrift.drift.DriftEvent;

import java.util.stream.Collectors;

/**
 * Formats a {@link DriftReport} into human-readable text suitable for CLI output.
 */
public class DriftReportFormatter {

    private static final String SEPARATOR = "─".repeat(60);

    public String format(DriftReport report) {
        StringBuilder sb = new StringBuilder();
        sb.append(SEPARATOR).append("\n");
        sb.append(String.format("LogDrift Report%n"));
        sb.append(String.format("  Baseline : %s%n", report.getBaselineVersion()));
        sb.append(String.format("  Compared : %s%n", report.getComparedVersion()));
        sb.append(String.format("  Generated: %s%n", report.getGeneratedAt()));
        sb.append(SEPARATOR).append("\n");

        if (!report.hasDrift()) {
            sb.append("  ✔ No schema drift detected.\n");
        } else {
            sb.append(String.format("  ✖ %d drift event(s) detected:%n%n", report.getDriftCount()));
            for (DriftEvent event : report.getEvents()) {
                sb.append(formatEvent(event));
            }
        }

        sb.append(SEPARATOR).append("\n");
        return sb.toString();
    }

    private String formatEvent(DriftEvent event) {
        return String.format("  [%s] field='%s'  %s%n",
                event.getDriftType(),
                event.getFieldName(),
                event.getDescription());
    }

    public String formatSummaryLine(DriftReport report) {
        if (!report.hasDrift()) {
            return "PASS: no drift between " + report.getBaselineVersion() + " and " + report.getComparedVersion();
        }
        return String.format("FAIL: %d drift event(s) between %s and %s",
                report.getDriftCount(), report.getBaselineVersion(), report.getComparedVersion());
    }
}
