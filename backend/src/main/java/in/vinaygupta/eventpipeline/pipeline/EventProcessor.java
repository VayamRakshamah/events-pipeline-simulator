package in.vinaygupta.eventpipeline.pipeline;

import in.vinaygupta.eventpipeline.broker.EventBroker;
import in.vinaygupta.eventpipeline.config.PipelineProperties;
import in.vinaygupta.eventpipeline.domain.EventScenario;
import in.vinaygupta.eventpipeline.domain.EventStatus;
import in.vinaygupta.eventpipeline.store.EventStore;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class EventProcessor {
    private final EventStore store;
    private final EventBroker broker;
    private final PipelineProperties properties;

    public EventProcessor(EventStore store, @Lazy EventBroker broker, PipelineProperties properties) {
        this.store = store;
        this.broker = broker;
        this.properties = properties;
    }

    public void process(String eventId) {
        delay();
        store.update(eventId, event -> event.transition(EventStatus.VALIDATING, "Worker validating inventory payload", activeTopic(event)));
        delay();

        var current = store.find(eventId).orElseThrow();
        if (current.scenario() == EventScenario.VALIDATION_FAILURE) {
            store.update(eventId, event -> event.failed(EventStatus.DEAD_LETTERED,
                    "Validation failed: SKU and store combination is not allowed",
                    properties.topics().dlq()));
            return;
        }

        store.update(eventId, event -> event.transition(EventStatus.PROCESSING, "Worker applying inventory delta", activeTopic(event)));
        delay();

        current = store.find(eventId).orElseThrow();
        if (current.scenario() == EventScenario.POISON_MESSAGE) {
            store.update(eventId, event -> event.failed(EventStatus.DEAD_LETTERED,
                    "Poison message detected after deserialization guard",
                    properties.topics().dlq()));
            broker.publishDeadLetter(eventId);
            return;
        }

        if (current.scenario() == EventScenario.TRANSIENT_FAILURE && current.retryCount() < properties.maxRetries()) {
            store.update(eventId, event -> event.retrying("Transient downstream timeout, scheduling retry", properties.topics().retry()));
            broker.publishRetry(eventId);
            return;
        }

        store.update(eventId, event -> event.transition(EventStatus.COMPLETED, "Inventory event completed", activeTopic(event)));
    }

    private String activeTopic(in.vinaygupta.eventpipeline.domain.InventoryEvent event) {
        if (event.retryCount() > 0) {
            return properties.topics().retry();
        }
        return properties.topics().inventory();
    }

    private void delay() {
        try {
            Thread.sleep(properties.workerDelay().toMillis());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }
}
