package in.vinaygupta.eventpipeline.pipeline;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import in.vinaygupta.eventpipeline.api.BurstRequest;
import in.vinaygupta.eventpipeline.api.InventoryEventRequest;
import in.vinaygupta.eventpipeline.broker.EventBroker;
import in.vinaygupta.eventpipeline.domain.EventScenario;
import in.vinaygupta.eventpipeline.domain.InventoryEvent;
import in.vinaygupta.eventpipeline.store.EventStore;
import org.springframework.stereotype.Service;

@Service
public class InventoryEventService {
    private final EventStore store;
    private final EventBroker broker;

    public InventoryEventService(EventStore store, EventBroker broker) {
        this.store = store;
        this.broker = broker;
    }

    public InventoryEvent publish(InventoryEventRequest request) {
        InventoryEvent event = InventoryEvent.received(
                "evt_" + UUID.randomUUID().toString().substring(0, 8),
                request.sku().toUpperCase(),
                request.storeId().toUpperCase(),
                request.quantityDelta(),
                request.scenario());
        store.save(event);
        broker.publish(event.eventId());
        return store.find(event.eventId()).orElse(event);
    }

    public List<InventoryEvent> publishBurst(BurstRequest request) {
        int count = Math.min(Math.max(request.count(), 1), 25);
        EventScenario[] scenarios = EventScenario.values();
        return IntStream.range(0, count)
                .mapToObj(index -> publish(new InventoryEventRequest(
                        "SKU-" + (4100 + index),
                        "BLR-" + ((index % 4) + 1),
                        index % 2 == 0 ? 8 : -3,
                        scenarios[index % scenarios.length]
                )))
                .toList();
    }
}
