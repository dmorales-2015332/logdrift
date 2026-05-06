package io.logdrift.tag;

import java.util.*;

/**
 * Application-level service for managing drift tags.
 * Validates tag constraints and delegates to the registry.
 */
public class DriftTagService {

    private static final int MAX_TAGS_PER_TARGET = 20;
    private static final int MAX_KEY_LENGTH = 64;
    private static final int MAX_VALUE_LENGTH = 128;

    private final DriftTagRegistry registry;

    public DriftTagService(DriftTagRegistry registry) {
        this.registry = Objects.requireNonNull(registry, "registry must not be null");
    }

    public void tag(String targetId, String key, String value, String appliedBy) {
        validateKey(key);
        validateValue(value);
        Set<DriftTag> existing = registry.getTagsFor(targetId);
        if (existing.size() >= MAX_TAGS_PER_TARGET) {
            throw new IllegalStateException(
                "Target '" + targetId + "' has reached the maximum tag limit of " + MAX_TAGS_PER_TARGET);
        }
        registry.addTag(targetId, new DriftTag(key, value, appliedBy, java.time.Instant.now()));
    }

    public void untag(String targetId, String key) {
        registry.removeTag(targetId, key);
    }

    public Set<DriftTag> getTagsFor(String targetId) {
        return registry.getTagsFor(targetId);
    }

    public List<String> findByTag(String key, String value) {
        return registry.findTargetsByTag(key, value);
    }

    public boolean isTagged(String targetId, String key) {
        return registry.hasTag(targetId, key);
    }

    private void validateKey(String key) {
        if (key == null || key.isBlank()) throw new IllegalArgumentException("Tag key must not be blank");
        if (key.length() > MAX_KEY_LENGTH)
            throw new IllegalArgumentException("Tag key exceeds max length of " + MAX_KEY_LENGTH);
    }

    private void validateValue(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Tag value must not be blank");
        if (value.length() > MAX_VALUE_LENGTH)
            throw new IllegalArgumentException("Tag value exceeds max length of " + MAX_VALUE_LENGTH);
    }
}
