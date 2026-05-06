package io.logdrift.suppress;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DriftSuppressionRegistryTest {

    private DriftSuppressionRegistry registry;
    private final Instant now = Instant.now();
    private final Instant future = now.plus(1, ChronoUnit.HOURS);
    private final Instant past = now.minus(1, ChronoUnit.HOURS);

    @BeforeEach
    void setUp() {
        registry = new DriftSuppressionRegistry();
    }

    @Test
    void register_addsSuppressionAndIsSuppressedReturnsTrue() {
        registry.register("order-service", "userId", future, "planned deploy", "alice");
        assertThat(registry.isSuppressed("order-service", "userId", now)).isTrue();
    }

    @Test
    void isSuppressed_returnsFalse_whenNoRulesMatch() {
        registry.register("order-service", "userId", future, "reason", "bob");
        assertThat(registry.isSuppressed("payment-service", "userId", now)).isFalse();
    }

    @Test
    void isSuppressed_returnsFalse_whenRuleExpired() {
        registry.register("order-service", "userId", past, "old rule", "carol");
        assertThat(registry.isSuppressed("order-service", "userId", now)).isFalse();
    }

    @Test
    void wildcardServicePattern_matchesMultipleServices() {
        registry.register("*-service", "traceId", future, "global", "dave");
        assertThat(registry.isSuppressed("order-service", "traceId", now)).isTrue();
        assertThat(registry.isSuppressed("payment-service", "traceId", now)).isTrue();
    }

    @Test
    void revoke_removesSuppressionById() {
        DriftSuppression s = registry.register("order-service", "userId", future, "test", "eve");
        assertThat(registry.revoke(s.getId())).isTrue();
        assertThat(registry.isSuppressed("order-service", "userId", now)).isFalse();
    }

    @Test
    void listActive_excludesExpiredRules() {
        registry.register("svc-a", "field1", future, "active", "frank");
        registry.register("svc-b", "field2", past, "expired", "frank");
        List<DriftSuppression> active = registry.listActive(now);
        assertThat(active).hasSize(1);
        assertThat(active.get(0).getServicePattern()).isEqualTo("svc-a");
    }

    @Test
    void purgeExpired_removesOnlyExpiredEntries() {
        registry.register("svc-a", "f1", future, "keep", "g");
        registry.register("svc-b", "f2", past, "remove", "g");
        int removed = registry.purgeExpired(now);
        assertThat(removed).isEqualTo(1);
        assertThat(registry.size()).isEqualTo(1);
    }
}
