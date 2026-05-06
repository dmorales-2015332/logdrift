package io.logdrift.export;

import io.logdrift.drift.DriftEvent;
import io.logdrift.report.DriftReport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DriftExportServiceTest {

    private DriftEvent event1;
    private DriftEvent event2;

    @BeforeEach
    void setUp() {
        event1 = mock(DriftEvent.class);
        when(event1.getServiceName()).thenReturn("order-service");
        when(event1.getFieldName()).thenReturn("userId");
        when(event1.getDriftType()).thenReturn("TYPE_CHANGE");
        when(event1.getDetectedAt()).thenReturn(Instant.parse("2024-06-01T10:00:00Z"));
        when(event1.getSeverity()).thenReturn("HIGH");

        event2 = mock(DriftEvent.class);
        when(event2.getServiceName()).thenReturn("payment,service");
        when(event2.getFieldName()).thenReturn("amount");
        when(event2.getDriftType()).thenReturn("FIELD_REMOVED");
        when(event2.getDetectedAt()).thenReturn(null);
        when(event2.getSeverity()).thenReturn("CRITICAL");
    }

    @Test
    void exportCsvWritesHeaderAndRows() throws IOException {
        DriftExportService service = new DriftExportService(DriftExportService.ExportFormat.CSV);
        StringWriter writer = new StringWriter();

        service.export(List.of(event1, event2), writer);

        String output = writer.toString();
        assertTrue(output.startsWith("service,field,driftType,detectedAt,severity\n"));
        assertTrue(output.contains("order-service,userId,TYPE_CHANGE,2024-06-01T10:00:00Z,HIGH"));
        assertTrue(output.contains("\"payment,service\",amount,FIELD_REMOVED,,CRITICAL"));
    }

    @Test
    void exportJsonlWritesOneObjectPerLine() throws IOException {
        DriftExportService service = new DriftExportService(DriftExportService.ExportFormat.JSONL);
        StringWriter writer = new StringWriter();

        service.export(List.of(event1), writer);

        String output = writer.toString().trim();
        assertTrue(output.startsWith("{"));
        assertTrue(output.contains("\"service\":\"order-service\""));
        assertTrue(output.contains("\"driftType\":\"TYPE_CHANGE\""));
        assertTrue(output.contains("\"severity\":\"HIGH\""));
    }

    @Test
    void exportReportDelegatesToEventList() throws IOException {
        DriftExportService service = new DriftExportService(DriftExportService.ExportFormat.CSV);
        DriftReport report = mock(DriftReport.class);
        when(report.getEvents()).thenReturn(List.of(event1));
        StringWriter writer = new StringWriter();

        service.exportReport(report, writer);

        assertTrue(writer.toString().contains("order-service"));
    }

    @Test
    void constructorRejectsNullFormat() {
        assertThrows(NullPointerException.class,
                () -> new DriftExportService(null));
    }

    @Test
    void exportRejectsNullEvents() {
        DriftExportService service = new DriftExportService(DriftExportService.ExportFormat.CSV);
        assertThrows(NullPointerException.class,
                () -> service.export(null, new StringWriter()));
    }
}
