package in.vinaygupta.eventpipeline.pipeline;

import static org.assertj.core.api.Assertions.assertThat;

import in.vinaygupta.eventpipeline.domain.EventScenario;
import in.vinaygupta.eventpipeline.domain.EventStatus;
import in.vinaygupta.eventpipeline.domain.InventoryEvent;
import in.vinaygupta.eventpipeline.metrics.PipelineMetrics;
import in.vinaygupta.eventpipeline.store.EventStore;
import org.junit.jupiter.api.Test;

class EventProcessorTest {
    private final EventStore store = new EventStore();
    private final EventProcessor processor = new EventProcessor(store);

    @Test
    void completesSuccessScenario() {
        InventoryEvent event = InventoryEvent.received("evt_1", "SKU-1", "BLR-1", 5, EventScenario.SUCCESS);
        store.save(event);

        processor.process("evt_1");

        assertThat(store.find("evt_1").orElseThrow().status()).isEqualTo(EventStatus.COMPLETED);
    }

    @Test
    void retriesTransientFailureBeforeCompletion() {
        InventoryEvent event = InventoryEvent.received("evt_2", "SKU-2", "BLR-1", 5, EventScenario.TRANSIENT_FAILURE);
        store.save(event);

        processor.process("evt_2");
        processor.process("evt_2");
        processor.process("evt_2");

        InventoryEvent updated = store.find("evt_2").orElseThrow();
        assertThat(updated.status()).isEqualTo(EventStatus.COMPLETED);
        assertThat(updated.retryCount()).isEqualTo(2);
        assertThat(updated.topicPath()).contains("inventory.events.retry");
    }

    @Test
    void routesValidationFailureToDeadLetter() {
        InventoryEvent event = InventoryEvent.received("evt_3", "SKU-3", "BLR-1", 5, EventScenario.VALIDATION_FAILURE);
        store.save(event);

        processor.process("evt_3");

        InventoryEvent updated = store.find("evt_3").orElseThrow();
        assertThat(updated.status()).isEqualTo(EventStatus.DEAD_LETTERED);
        assertThat(updated.failureReason()).contains("Validation failed");
        assertThat(updated.topicPath()).contains("inventory.events.dlq");
    }

    @Test
    void routesPoisonMessageToDeadLetter() {
        InventoryEvent event = InventoryEvent.received("evt_4", "SKU-4", "BLR-1", 5, EventScenario.POISON_MESSAGE);
        store.save(event);

        processor.process("evt_4");

        InventoryEvent updated = store.find("evt_4").orElseThrow();
        assertThat(updated.status()).isEqualTo(EventStatus.DEAD_LETTERED);
        assertThat(updated.failureReason()).contains("Poison message");
    }

    @Test
    void calculatesPipelineMetricsFromStoredEvents() {
        store.save(InventoryEvent.received("evt_5", "SKU-5", "BLR-1", 5, EventScenario.SUCCESS));
        store.save(InventoryEvent.received("evt_6", "SKU-6", "BLR-1", 5, EventScenario.POISON_MESSAGE));

        processor.process("evt_5");
        processor.process("evt_6");

        var snapshot = new PipelineMetrics(store).snapshot();

        assertThat(snapshot.totalEvents()).isEqualTo(2);
        assertThat(snapshot.completedEvents()).isEqualTo(1);
        assertThat(snapshot.deadLetteredEvents()).isEqualTo(1);
        assertThat(snapshot.successRate()).isEqualTo(0.5);
    }
}
