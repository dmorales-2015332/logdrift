package io.logdrift.pipeline;

import io.logdrift.drift.DriftEvent;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DriftPipelineTest {

    private DriftEvent mockEvent(String id) {
        DriftEvent e = mock(DriftEvent.class);
        when(e.toString()).thenReturn("Event[" + id + "]");
        return e;
    }

    @Test
    void runExecutesAllEnabledStagesInOrder() {
        List<String> order = new ArrayList<>();

        DriftPipelineStage s1 = new DriftPipelineStage() {
            public String getName() { return "stage-1"; }
            public List<DriftEvent> process(List<DriftEvent> events) {
                order.add("stage-1");
                return events;
            }
        };
        DriftPipelineStage s2 = new DriftPipelineStage() {
            public String getName() { return "stage-2"; }
            public List<DriftEvent> process(List<DriftEvent> events) {
                order.add("stage-2");
                return events;
            }
        };

        DriftPipeline pipeline = DriftPipelineBuilder.create()
                .withName("test-pipeline")
                .addStage(s1)
                .addStage(s2)
                .build();

        DriftEvent event = mockEvent("e1");
        DriftPipelineResult result = pipeline.run(List.of(event));

        assertEquals(List.of("stage-1", "stage-2"), order);
        assertEquals(2, result.getExecutedStages().size());
        assertEquals(1, result.getEventCount());
        assertEquals("test-pipeline", result.getPipelineName());
    }

    @Test
    void runSkipsDisabledStages() {
        DriftPipelineStage disabled = new DriftPipelineStage() {
            public String getName() { return "disabled-stage"; }
            public boolean isEnabled() { return false; }
            public List<DriftEvent> process(List<DriftEvent> events) {
                fail("Disabled stage should not be called");
                return events;
            }
        };
        DriftPipelineStage active = new DriftPipelineStage() {
            public String getName() { return "active-stage"; }
            public List<DriftEvent> process(List<DriftEvent> events) { return events; }
        };

        DriftPipeline pipeline = DriftPipelineBuilder.create()
                .withName("skip-test")
                .addStage(disabled)
                .addStage(active)
                .build();

        DriftPipelineResult result = pipeline.run(List.of(mockEvent("e2")));
        assertEquals(List.of("active-stage"), result.getExecutedStages());
    }

    @Test
    void builderThrowsWhenNameMissing() {
        assertThrows(IllegalStateException.class, () ->
                DriftPipelineBuilder.create()
                        .addStage(new DriftPipelineStage() {
                            public String getName() { return "s"; }
                            public List<DriftEvent> process(List<DriftEvent> e) { return e; }
                        })
                        .build());
    }

    @Test
    void builderThrowsWhenNoStages() {
        assertThrows(IllegalStateException.class, () ->
                DriftPipelineBuilder.create().withName("empty").build());
    }

    @Test
    void resultIsEmptyWhenNoEventsProduced() {
        DriftPipelineStage drain = new DriftPipelineStage() {
            public String getName() { return "drain"; }
            public List<DriftEvent> process(List<DriftEvent> events) { return List.of(); }
        };
        DriftPipeline pipeline = DriftPipelineBuilder.create()
                .withName("drain-pipeline")
                .addStage(drain)
                .build();

        DriftPipelineResult result = pipeline.run(List.of(mockEvent("e3")));
        assertTrue(result.isEmpty());
        assertEquals(0, result.getEventCount());
    }
}
