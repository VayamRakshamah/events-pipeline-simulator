package in.vinaygupta.eventpipeline.api;

import java.time.Instant;
import java.util.List;

import in.vinaygupta.eventpipeline.domain.EventScenario;
import in.vinaygupta.eventpipeline.domain.EventStatus;
import in.vinaygupta.eventpipeline.domain.InventoryEvent;
import in.vinaygupta.eventpipeline.domain.TimelineEntry;

public record EventResponse(
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
        long latencyMs,
        Instant createdAt,
        Instant updatedAt
) {
    public static EventResponse from(InventoryEvent event) {
        return new EventResponse(event.eventId(), event.sku(), event.storeId(), event.quantityDelta(),
                event.scenario(), event.status(), event.retryCount(), event.failureReason(), event.topicPath(),
                event.timeline(), event.latencyMs(), event.createdAt(), event.updatedAt());
    }
}
