package io.logdrift.tag;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Stores and manages drift tags keyed by a target identifier
 * (e.g., a drift event ID or snapshot ID).
 */
public class DriftTagRegistry {

    private final Map<String, Set<DriftTag>> tagsByTarget = new ConcurrentHashMap<>();

    public void addTag(String targetId, DriftTag tag) {
        Objects.requireNonNull(targetId, "targetId must not be null");
        Objects.requireNonNull(tag, "tag must not be null");
        tagsByTarget.computeIfAbsent(targetId, k -> ConcurrentHashMap.newKeySet()).add(tag);
    }

    public void removeTag(String targetId, String key) {
        Set<DriftTag> tags = tagsByTarget.get(targetId);
        if (tags != null) {
            tags.removeIf(t -> t.getKey().equals(key));
        }
    }

    public Set<DriftTag> getTagsFor(String targetId) {
        return Collections.unmodifiableSet(
            tagsByTarget.getOrDefault(targetId, Collections.emptySet())
        );
    }

    public List<String> findTargetsByTag(String key, String value) {
        return tagsByTarget.entrySet().stream()
            .filter(e -> e.getValue().stream()
                .anyMatch(t -> t.getKey().equals(key) && t.getValue().equals(value)))
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }

    public void clearTagsFor(String targetId) {
        tagsByTarget.remove(targetId);
    }

    public boolean hasTag(String targetId, String key) {
        return tagsByTarget.getOrDefault(targetId, Collections.emptySet())
            .stream().anyMatch(t -> t.getKey().equals(key));
    }

    public int totalTaggedTargets() {
        return tagsByTarget.size();
    }
}
