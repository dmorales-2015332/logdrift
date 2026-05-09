package io.logdrift.pipeline;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Fluent builder for constructing a {@link DriftPipeline} with an ordered set
 * of processing stages.
 */
public class DriftPipelineBuilder {

    private String name;
    private final List<DriftPipelineStage> stages = new ArrayList<>();

    private DriftPipelineBuilder() {}

    public static DriftPipelineBuilder create() {
        return new DriftPipelineBuilder();
    }

    public DriftPipelineBuilder withName(String name) {
        this.name = Objects.requireNonNull(name, "pipeline name must not be null");
        return this;
    }

    public DriftPipelineBuilder addStage(DriftPipelineStage stage) {
        Objects.requireNonNull(stage, "stage must not be null");
        stages.add(stage);
        return this;
    }

    public DriftPipeline build() {
        if (name == null || name.isBlank()) {
            throw new IllegalStateException("Pipeline name must be set before building");
        }
        if (stages.isEmpty()) {
            throw new IllegalStateException("Pipeline must have at least one stage");
        }
        return new DriftPipeline(name, stages);
    }
}
