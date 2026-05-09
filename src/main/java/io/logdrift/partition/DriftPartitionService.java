package io.logdrift.partition;

import io.logdrift.drift.DriftEvent;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Partitions drift events into named buckets based on a configurable key strategy.
 * Useful for isolating drift per service, environment, or field set.
 */
public class DriftPartitionService {

    private final DriftPartitionStrategy strategy;

    public DriftPartitionService(DriftPartitionStrategy strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException("Partition strategy must not be null");
        }
        this.strategy = strategy;
    }

    /**
     * Partitions the given list of drift events into a map keyed by partition label.
     *
     * @param events the drift events to partition
     * @return a map from partition label to list of events in that partition
     */
    public Map<String, List<DriftEvent>> partition(List<DriftEvent> events) {
        if (events == null || events.isEmpty()) {
            return Collections.emptyMap();
        }
        return events.stream()
                .collect(Collectors.groupingBy(
                        event -> strategy.resolvePartitionKey(event),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
    }

    /**
     * Returns the number of distinct partitions that would result from the given events.
     *
     * @param events the drift events to evaluate
     * @return count of distinct partition keys
     */
    public int countPartitions(List<DriftEvent> events) {
        return partition(events).size();
    }

    /**
     * Returns events belonging to a specific partition label.
     *
     * @param events         all drift events
     * @param partitionLabel the label to filter by
     * @return events in the requested partition, or empty list if none
     */
    public List<DriftEvent> getPartition(List<DriftEvent> events, String partitionLabel) {
        return partition(events).getOrDefault(partitionLabel, Collections.emptyList());
    }

    public DriftPartitionStrategy getStrategy() {
        return strategy;
    }
}
