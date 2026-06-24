package in.vinaygupta.eventpipeline.domain;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public record InventoryEvent(
        String eventId,
        String sku,
        String storeId,
        int quantityDelta,
        EventScenario scenario,
        EventStatus status,
        int retryCount,
        String failureReason,
        List<String> topicPath,
        List<TimelineEntry> timeline,
        Instant createdAt,
        Instant updatedAt,
        Instant completedAt
) {
    public static InventoryEvent received(String eventId, String sku, String storeId, int quantityDelta,
                                          EventScenario scenario) {
        Instant now = Instant.now();
        return new InventoryEvent(
                eventId,
                sku,
                storeId,
                quantityDelta,
                scenario,
                EventStatus.RECEIVED,
                0,
                null,
                List.of(),
                List.of(TimelineEntry.of(EventStatus.RECEIVED, "Request accepted by API", null)),
                now,
                now,
                null
        );
    }

    public InventoryEvent transition(EventStatus nextStatus, String message, String topic) {
        List<TimelineEntry> nextTimeline = new ArrayList<>(timeline);
        nextTimeline.add(TimelineEntry.of(nextStatus, message, topic));

        List<String> nextTopicPath = new ArrayList<>(topicPath);
        if (topic != null && !topic.isBlank() && (nextTopicPath.isEmpty() || !nextTopicPath.get(nextTopicPath.size() - 1).equals(topic))) {
            nextTopicPath.add(topic);
        }

        Instant now = Instant.now();
        Instant doneAt = switch (nextStatus) {
            case COMPLETED, FAILED, DEAD_LETTERED -> now;
            default -> completedAt;
        };

        return new InventoryEvent(eventId, sku, storeId, quantityDelta, scenario, nextStatus, retryCount,
                failureReason, List.copyOf(nextTopicPath), List.copyOf(nextTimeline), createdAt, now, doneAt);
    }

    public InventoryEvent retrying(String message, String retryTopic) {
        InventoryEvent transitioned = transition(EventStatus.RETRYING, message, retryTopic);
        return new InventoryEvent(eventId, sku, storeId, quantityDelta, scenario, transitioned.status,
                retryCount + 1, failureReason, transitioned.topicPath, transitioned.timeline, createdAt,
                transitioned.updatedAt, transitioned.completedAt);
    }

    public InventoryEvent failed(EventStatus terminalStatus, String reason, String topic) {
        InventoryEvent transitioned = transition(terminalStatus, reason, topic);
        return new InventoryEvent(eventId, sku, storeId, quantityDelta, scenario, transitioned.status,
                transitioned.retryCount, reason, transitioned.topicPath, transitioned.timeline, createdAt,
                transitioned.updatedAt, transitioned.completedAt);
    }

    public long latencyMs() {
        Instant end = completedAt == null ? updatedAt : completedAt;
        return Duration.between(createdAt, end).toMillis();
    }
}
