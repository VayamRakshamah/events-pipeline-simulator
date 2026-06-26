package in.vinaygupta.eventpipeline.api;

import java.util.List;

import in.vinaygupta.eventpipeline.metrics.PipelineMetrics;
import in.vinaygupta.eventpipeline.metrics.PipelineMetricsSnapshot;
import in.vinaygupta.eventpipeline.pipeline.InventoryEventService;
import in.vinaygupta.eventpipeline.store.EventStore;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class EventController {
    private final InventoryEventService service;
    private final EventStore store;
    private final PipelineMetrics metrics;

    public EventController(InventoryEventService service, EventStore store, PipelineMetrics metrics) {
        this.service = service;
        this.store = store;
        this.metrics = metrics;
    }

    @PostMapping("/events/inventory")
    public EventResponse publish(@Valid @RequestBody InventoryEventRequest request) {
        return EventResponse.from(service.publish(request));
    }

    @PostMapping("/scenarios/burst")
    public List<EventResponse> burst(@Valid @RequestBody BurstRequest request) {
        return service.publishBurst(request).stream().map(EventResponse::from).toList();
    }

    @GetMapping("/events")
    public List<EventResponse> events() {
        return store.recent().stream().map(EventResponse::from).toList();
    }

    @GetMapping("/events/{eventId}")
    public ResponseEntity<EventResponse> event(@PathVariable String eventId) {
        return store.find(eventId)
                .map(EventResponse::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/metrics/pipeline")
    public PipelineMetricsSnapshot metrics() {
        return metrics.snapshot();
    }

    @DeleteMapping("/events")
    public ResponseEntity<Void> clear() {
        store.clear();
        return ResponseEntity.noContent().build();
    }
}
