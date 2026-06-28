package in.vinaygupta.eventpipeline.api;

import java.time.Duration;

import in.vinaygupta.eventpipeline.store.EventStore;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/stream")
public class EventStreamController {
    private final EventStore store;

    public EventStreamController(EventStore store) {
        this.store = store;
    }

    @GetMapping(value = "/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter events() {
        SseEmitter emitter = new SseEmitter(Duration.ofMinutes(10).toMillis());
        Thread streamThread = new Thread(() -> {
            try {
                for (int i = 0; i < 600; i++) {
                    emitter.send(SseEmitter.event()
                            .name("events")
                            .data(store.recent().stream().map(EventResponse::from).toList()));
                    Thread.sleep(1000);
                }
                emitter.complete();
            } catch (Exception exception) {
                emitter.completeWithError(exception);
            }
        }, "event-stream");
        streamThread.setDaemon(true);
        streamThread.start();
        return emitter;
    }
}
