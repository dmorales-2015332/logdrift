package io.logdrift.audit;

import io.logdrift.drift.DriftEvent;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Service responsible for recording and querying drift audit logs.
 * Maintains an in-memory audit trail keyed by serviceId.
 */
public class DriftAuditService {

    private final Map<String, List<DriftAuditLog>> auditStore = new ConcurrentHashMap<>();

    /**
     * Records a new audit log entry derived from a list of drift events.
     *
     * @param serviceId         the service being audited
     * @param deploymentVersion the deployment version being assessed
     * @param events            detected drift events for this run
     * @return the created {@link DriftAuditLog}
     */
    public DriftAuditLog record(String serviceId, String deploymentVersion, List<DriftEvent> events) {
        Objects.requireNonNull(serviceId, "serviceId must not be null");
        Objects.requireNonNull(deploymentVersion, "deploymentVersion must not be null");
        Objects.requireNonNull(events, "events must not be null");

        List<String> summaries = events.stream()
                .map(DriftEvent::getSummary)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        DriftAuditLog.AuditSeverity severity = resolveSeverity(events);
        DriftAuditLog log = new DriftAuditLog(serviceId, deploymentVersion, Instant.now(), summaries, severity);

        auditStore.computeIfAbsent(serviceId, k -> new ArrayList<>()).add(log);
        return log;
    }

    /**
     * Returns all audit logs for a given service, ordered oldest-first.
     */
    public List<DriftAuditLog> getAuditHistory(String serviceId) {
        return List.copyOf(auditStore.getOrDefault(serviceId, List.of()));
    }

    /**
     * Returns all audit logs across every service.
     */
    public List<DriftAuditLog> getAllAuditLogs() {
        return auditStore.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    /**
     * Clears the audit history for a specific service.
     */
    public void clearHistory(String serviceId) {
        auditStore.remove(serviceId);
    }

    private DriftAuditLog.AuditSeverity resolveSeverity(List<DriftEvent> events) {
        if (events.isEmpty()) {
            return DriftAuditLog.AuditSeverity.NONE;
        }
        long criticalCount = events.stream().filter(e -> e.isCritical()).count();
        if (criticalCount > 0) return DriftAuditLog.AuditSeverity.CRITICAL;
        if (events.size() >= 10) return DriftAuditLog.AuditSeverity.HIGH;
        if (events.size() >= 5) return DriftAuditLog.AuditSeverity.MEDIUM;
        return DriftAuditLog.AuditSeverity.LOW;
    }
}
