package io.logdrift.pipeline;

import io.logdrift.drift.DriftEvent;

import java.util.List;
import java.util.Objects;

/**
 * Immutable result produced by a {@link DriftPipeline} run, containing the
 * processed events and metadata about which stages were executed.
 */
public class DriftPipelineResult {

    private final String pipelineName;
    private final List<DriftEvent> events;
    private final List<String> executedStages;

    public DriftPipelineResult(String pipelineName, List<DriftEvent> events,
                               List<String> executedStages) {
        this.pipelineName = Objects.requireNonNull(pipelineName);
        this.events = Objects.requireNonNull(events);
        this.executedStages = Objects.requireNonNull(executedStages);
    }

    public String getPipelineName() {
        return pipelineName;
    }

    public List<DriftEvent> getEvents() {
        return events;
    }

    public List<String> getExecutedStages() {
        return executedStages;
    }

    public int getEventCount() {
        return events.size();
    }

    public boolean isEmpty() {
        return events.isEmpty();
    }

    @Override
    public String toString() {
        return "DriftPipelineResult{pipeline='" + pipelineName +
                "', events=" + events.size() +
                ", stages=" + executedStages + '}';
    }
}
