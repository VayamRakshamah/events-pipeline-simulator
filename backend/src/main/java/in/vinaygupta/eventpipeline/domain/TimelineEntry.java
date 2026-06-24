package in.vinaygupta.eventpipeline.domain;

import java.time.Instant;

public record TimelineEntry(
        EventStatus status,
        String message,
        String topic,
        Instant timestamp
) {
    public static TimelineEntry of(EventStatus status, String message, String topic) {
        return new TimelineEntry(status, message, topic, Instant.now());
    }
}
