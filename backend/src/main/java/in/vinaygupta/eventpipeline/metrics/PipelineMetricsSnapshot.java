package in.vinaygupta.eventpipeline.metrics;

public record PipelineMetricsSnapshot(
        long totalEvents,
        long completedEvents,
        long retriedEvents,
        long deadLetteredEvents,
        double successRate,
        long averageLatencyMs
) {
}
