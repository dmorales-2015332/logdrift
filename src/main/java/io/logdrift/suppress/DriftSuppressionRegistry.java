package io.logdrift.suppress;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry that stores and evaluates active drift suppression rules.
 */
public class DriftSuppressionRegistry {

    private final Map<String, DriftSuppression> suppressions = new ConcurrentHashMap<>();

    public DriftSuppression register(String servicePattern, String fieldPattern,
                                     Instant expiresAt, String reason, String createdBy) {
        String id = UUID.randomUUID().toString();
        DriftSuppression suppression = new DriftSuppression(
                id, servicePattern, fieldPattern, expiresAt, reason, createdBy);
        suppressions.put(id, suppression);
        return suppression;
    }

    public boolean isSuppressed(String service, String field, Instant now) {
        purgeExpired(now);
        return suppressions.values().stream()
                .anyMatch(s -> !s.isExpired(now) && s.matches(service, field));
    }

    public Optional<DriftSuppression> findById(String id) {
        return Optional.ofNullable(suppressions.get(id));
    }

    public boolean revoke(String id) {
        return suppressions.remove(id) != null;
    }

    public List<DriftSuppression> listActive(Instant now) {
        purgeExpired(now);
        return Collections.unmodifiableList(new ArrayList<>(suppressions.values()));
    }

    public int purgeExpired(Instant now) {
        List<String> expired = suppressions.entrySet().stream()
                .filter(e -> e.getValue().isExpired(now))
                .map(Map.Entry::getKey)
                .toList();
        expired.forEach(suppressions::remove);
        return expired.size();
    }

    public int size() {
        return suppressions.size();
    }
}
