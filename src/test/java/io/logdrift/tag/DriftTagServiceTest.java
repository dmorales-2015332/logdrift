package io.logdrift.tag;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DriftTagServiceTest {

    private DriftTagRegistry registry;
    private DriftTagService service;

    @BeforeEach
    void setUp() {
        registry = new DriftTagRegistry();
        service = new DriftTagService(registry);
    }

    @Test
    void tag_shouldAddTagSuccessfully() {
        service.tag("snap-1", "env", "prod", "alice");
        Set<DriftTag> tags = service.getTagsFor("snap-1");
        assertEquals(1, tags.size());
        DriftTag tag = tags.iterator().next();
        assertEquals("env", tag.getKey());
        assertEquals("prod", tag.getValue());
        assertEquals("alice", tag.getAppliedBy());
    }

    @Test
    void tag_shouldRejectBlankKey() {
        assertThrows(IllegalArgumentException.class,
            () -> service.tag("snap-2", "", "val", "bob"));
    }

    @Test
    void tag_shouldRejectKeyExceedingMaxLength() {
        String longKey = "k".repeat(65);
        assertThrows(IllegalArgumentException.class,
            () -> service.tag("snap-3", longKey, "val", "bob"));
    }

    @Test
    void tag_shouldEnforceMaxTagsPerTarget() {
        for (int i = 0; i < 20; i++) {
            service.tag("snap-4", "key" + i, "val" + i, "system");
        }
        assertThrows(IllegalStateException.class,
            () -> service.tag("snap-4", "overflow", "x", "system"));
    }

    @Test
    void untag_shouldRemoveTagByKey() {
        service.tag("snap-5", "team", "infra", "system");
        service.untag("snap-5", "team");
        assertFalse(service.isTagged("snap-5", "team"));
    }

    @Test
    void findByTag_shouldReturnCorrectTargets() {
        service.tag("snap-6", "env", "prod", "system");
        service.tag("snap-7", "env", "staging", "system");
        service.tag("snap-8", "env", "prod", "system");

        List<String> prodTargets = service.findByTag("env", "prod");
        assertEquals(2, prodTargets.size());
        assertTrue(prodTargets.contains("snap-6"));
        assertTrue(prodTargets.contains("snap-8"));
    }

    @Test
    void isTagged_shouldReturnFalseForUnknownTarget() {
        assertFalse(service.isTagged("nonexistent", "env"));
    }
}
