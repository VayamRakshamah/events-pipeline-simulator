package in.vinaygupta.eventpipeline.metrics;

import in.vinaygupta.eventpipeline.domain.EventStatus;
import in.vinaygupta.eventpipeline.store.EventStore;
import org.springframework.stereotype.Component;

@Component
public class PipelineMetrics {
    private final EventStore store;

    public PipelineMetrics(EventStore store) {
        this.store = store;
    }

    public PipelineMetricsSnapshot snapshot() {
        var events = store.all();
        long total = events.size();
        long completed = events.stream().filter(event -> event.status() == EventStatus.COMPLETED).count();
        long retrying = events.stream().filter(event -> event.retryCount() > 0).count();
        long deadLettered = events.stream().filter(event -> event.status() == EventStatus.DEAD_LETTERED).count();
        long avgLatency = events.stream()
                .filter(event -> event.completedAt() != null)
                .mapToLong(in.vinaygupta.eventpipeline.domain.InventoryEvent::latencyMs)
                .map(latency -> latency)
                .average()
                .stream()
                .mapToLong(Math::round)
                .findFirst()
                .orElse(0L);
        double successRate = total == 0 ? 0 : (double) completed / total;
        return new PipelineMetricsSnapshot(total, completed, retrying, deadLettered, successRate, avgLatency);
    }
}
