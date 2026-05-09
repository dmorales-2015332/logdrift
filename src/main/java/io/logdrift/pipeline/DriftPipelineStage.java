package io.logdrift.pipeline;

import io.logdrift.drift.DriftEvent;

import java.util.List;

/**
 * Represents a single processing stage in a drift event pipeline.
 * Each stage receives a list of events and returns a (possibly transformed) list.
 */
public interface DriftPipelineStage {

    /**
     * Returns the unique name of this stage, used for logging and metrics.
     */
    String getName();

    /**
     * Processes the given drift events and returns the result.
     *
     * @param events input events for this stage
     * @return processed events to pass to the next stage
     */
    List<DriftEvent> process(List<DriftEvent> events);

    /**
     * Whether this stage is enabled. Disabled stages are skipped during execution.
     */
    default boolean isEnabled() {
        return true;
    }
}
