package io.logdrift.tag;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DriftTagRegistryTest {

    private DriftTagRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new DriftTagRegistry();
    }

    @Test
    void addTag_shouldStoreTagForTarget() {
        registry.addTag("event-1", new DriftTag("env", "prod"));
        Set<DriftTag> tags = registry.getTagsFor("event-1");
        assertEquals(1, tags.size());
        assertEquals("env", tags.iterator().next().getKey());
    }

    @Test
    void addTag_shouldNotStoreDuplicateTags() {
        registry.addTag("event-1", new DriftTag("env", "prod"));
        registry.addTag("event-1", new DriftTag("env", "prod"));
        assertEquals(1, registry.getTagsFor("event-1").size());
    }

    @Test
    void removeTag_shouldDeleteByKey() {
        registry.addTag("event-2", new DriftTag("team", "payments"));
        registry.removeTag("event-2", "team");
        assertTrue(registry.getTagsFor("event-2").isEmpty());
    }

    @Test
    void findTargetsByTag_shouldReturnMatchingTargets() {
        registry.addTag("event-3", new DriftTag("env", "staging"));
        registry.addTag("event-4", new DriftTag("env", "prod"));
        registry.addTag("event-5", new DriftTag("env", "staging"));

        List<String> results = registry.findTargetsByTag("env", "staging");
        assertEquals(2, results.size());
        assertTrue(results.contains("event-3"));
        assertTrue(results.contains("event-5"));
    }

    @Test
    void hasTag_shouldReturnTrueWhenKeyPresent() {
        registry.addTag("event-6", new DriftTag("critical", "true"));
        assertTrue(registry.hasTag("event-6", "critical"));
        assertFalse(registry.hasTag("event-6", "env"));
    }

    @Test
    void clearTagsFor_shouldRemoveAllTagsForTarget() {
        registry.addTag("event-7", new DriftTag("a", "1"));
        registry.addTag("event-7", new DriftTag("b", "2"));
        registry.clearTagsFor("event-7");
        assertTrue(registry.getTagsFor("event-7").isEmpty());
        assertEquals(0, registry.totalTaggedTargets());
    }

    @Test
    void getTagsFor_unknownTarget_shouldReturnEmptySet() {
        assertTrue(registry.getTagsFor("unknown").isEmpty());
    }
}
