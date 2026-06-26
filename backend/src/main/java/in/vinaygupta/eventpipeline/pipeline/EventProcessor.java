package in.vinaygupta.eventpipeline.pipeline;

import in.vinaygupta.eventpipeline.domain.EventScenario;
import in.vinaygupta.eventpipeline.domain.EventStatus;
import in.vinaygupta.eventpipeline.domain.InventoryEvent;
import in.vinaygupta.eventpipeline.store.EventStore;
import org.springframework.stereotype.Service;

@Service
public class EventProcessor {
    private static final int MAX_RETRIES = 2;
    private static final String INVENTORY_TOPIC = "inventory.events";
    private static final String RETRY_TOPIC = "inventory.events.retry";
    private static final String DLQ_TOPIC = "inventory.events.dlq";

    private final EventStore store;

    public EventProcessor(EventStore store) {
        this.store = store;
    }

    public void process(String eventId) {
        store.update(eventId, event -> event.transition(EventStatus.VALIDATING,
                "Worker validating inventory payload", activeTopic(event)));

        InventoryEvent current = store.find(eventId).orElseThrow();
        if (current.scenario() == EventScenario.VALIDATION_FAILURE) {
            store.update(eventId, event -> event.failed(EventStatus.DEAD_LETTERED,
                    "Validation failed: SKU and store combination is not allowed",
                    DLQ_TOPIC));
            return;
        }

        store.update(eventId, event -> event.transition(EventStatus.PROCESSING,
                "Worker applying inventory delta", activeTopic(event)));

        current = store.find(eventId).orElseThrow();
        if (current.scenario() == EventScenario.POISON_MESSAGE) {
            store.update(eventId, event -> event.failed(EventStatus.DEAD_LETTERED,
                    "Poison message detected after deserialization guard",
                    DLQ_TOPIC));
            return;
        }

        if (current.scenario() == EventScenario.TRANSIENT_FAILURE && current.retryCount() < MAX_RETRIES) {
            store.update(eventId, event -> event.retrying(
                    "Transient downstream timeout, scheduling retry",
                    RETRY_TOPIC));
            return;
        }

        store.update(eventId, event -> event.transition(EventStatus.COMPLETED,
                "Inventory event completed", activeTopic(event)));
    }

    private String activeTopic(InventoryEvent event) {
        if (event.retryCount() > 0) {
            return RETRY_TOPIC;
        }
        return INVENTORY_TOPIC;
    }
}
