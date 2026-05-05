package io.logdrift.audit;

import io.logdrift.drift.DriftEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DriftAuditServiceTest {

    private DriftAuditService auditService;

    @BeforeEach
    void setUp() {
        auditService = new DriftAuditService();
    }

    @Test
    void record_withNoDriftEvents_producesNoneSeverityLog() {
        DriftAuditLog log = auditService.record("svc-alpha", "v1.0.0", List.of());

        assertNotNull(log);
        assertEquals("svc-alpha", log.getServiceId());
        assertEquals("v1.0.0", log.getDeploymentVersion());
        assertEquals(DriftAuditLog.AuditSeverity.NONE, log.getSeverity());
        assertFalse(log.hasDrift());
    }

    @Test
    void record_withFewDriftEvents_producesLowSeverityLog() {
        DriftEvent e1 = mock(DriftEvent.class);
        when(e1.getSummary()).thenReturn("field 'timestamp' type changed");
        when(e1.isCritical()).thenReturn(false);

        DriftAuditLog log = auditService.record("svc-beta", "v2.1.0", List.of(e1));

        assertEquals(DriftAuditLog.AuditSeverity.LOW, log.getSeverity());
        assertTrue(log.hasDrift());
        assertEquals(1, log.getDriftSummaries().size());
    }

    @Test
    void record_withCriticalEvent_producesCriticalSeverityLog() {
        DriftEvent critical = mock(DriftEvent.class);
        when(critical.getSummary()).thenReturn("schema completely replaced");
        when(critical.isCritical()).thenReturn(true);

        DriftAuditLog log = auditService.record("svc-gamma", "v3.0.0", List.of(critical));

        assertEquals(DriftAuditLog.AuditSeverity.CRITICAL, log.getSeverity());
    }

    @Test
    void getAuditHistory_returnsLogsInInsertionOrder() {
        auditService.record("svc-delta", "v1.0.0", List.of());
        auditService.record("svc-delta", "v1.1.0", List.of());
        auditService.record("svc-delta", "v1.2.0", List.of());

        List<DriftAuditLog> history = auditService.getAuditHistory("svc-delta");

        assertEquals(3, history.size());
        assertEquals("v1.0.0", history.get(0).getDeploymentVersion());
        assertEquals("v1.2.0", history.get(2).getDeploymentVersion());
    }

    @Test
    void getAuditHistory_forUnknownService_returnsEmptyList() {
        List<DriftAuditLog> history = auditService.getAuditHistory("unknown-svc");
        assertNotNull(history);
        assertTrue(history.isEmpty());
    }

    @Test
    void clearHistory_removesAllLogsForService() {
        auditService.record("svc-epsilon", "v1.0.0", List.of());
        auditService.clearHistory("svc-epsilon");

        assertTrue(auditService.getAuditHistory("svc-epsilon").isEmpty());
    }

    @Test
    void getAllAuditLogs_aggregatesAcrossServices() {
        auditService.record("svc-a", "v1", List.of());
        auditService.record("svc-b", "v1", List.of());

        assertEquals(2, auditService.getAllAuditLogs().size());
    }
}
