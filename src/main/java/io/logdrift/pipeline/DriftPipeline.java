package io.logdrift.pipeline;

import io.logdrift.drift.DriftEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Orchestrates a sequence of {@link DriftPipelineStage} instances applied to
 * drift events in order. Stages that are disabled are transparently skipped.
 */
public class DriftPipeline {

    private final String name;
    private final List<DriftPipelineStage> stages;

    public DriftPipeline(String name, List<DriftPipelineStage> stages) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.stages = Collections.unmodifiableList(new ArrayList<>(stages));
    }

    public String getName() {
        return name;
    }

    public List<DriftPipelineStage> getStages() {
        return stages;
    }

    /**
     * Runs all enabled stages in order and returns the final list of events.
     *
     * @param input initial drift events
     * @return events after all pipeline stages have been applied
     */
    public DriftPipelineResult run(List<DriftEvent> input) {
        Objects.requireNonNull(input, "input must not be null");
        List<DriftEvent> current = new ArrayList<>(input);
        List<String> executedStages = new ArrayList<>();

        for (DriftPipelineStage stage : stages) {
            if (stage.isEnabled()) {
                current = stage.process(current);
                executedStages.add(stage.getName());
            }
        }

        return new DriftPipelineResult(name, Collections.unmodifiableList(current),
                Collections.unmodifiableList(executedStages));
    }
}
