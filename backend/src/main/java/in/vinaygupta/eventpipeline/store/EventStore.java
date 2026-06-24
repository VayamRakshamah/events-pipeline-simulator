package in.vinaygupta.eventpipeline.store;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.UnaryOperator;

import in.vinaygupta.eventpipeline.domain.InventoryEvent;
import org.springframework.stereotype.Component;

@Component
public class EventStore {
    private final ConcurrentHashMap<String, InventoryEvent> events = new ConcurrentHashMap<>();

    public InventoryEvent save(InventoryEvent event) {
        events.put(event.eventId(), event);
        return event;
    }

    public InventoryEvent update(String eventId, UnaryOperator<InventoryEvent> updater) {
        return events.computeIfPresent(eventId, (ignored, current) -> updater.apply(current));
    }

    public Optional<InventoryEvent> find(String eventId) {
        return Optional.ofNullable(events.get(eventId));
    }

    public List<InventoryEvent> recent() {
        return events.values().stream()
                .sorted(Comparator.comparing(InventoryEvent::createdAt).reversed())
                .limit(100)
                .toList();
    }

    public List<InventoryEvent> all() {
        return new ArrayList<>(events.values());
    }

    public void clear() {
        events.clear();
    }
}
