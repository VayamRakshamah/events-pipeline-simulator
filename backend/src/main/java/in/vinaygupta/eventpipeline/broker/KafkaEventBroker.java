package in.vinaygupta.eventpipeline.broker;

import in.vinaygupta.eventpipeline.config.PipelineProperties;
import in.vinaygupta.eventpipeline.domain.EventStatus;
import in.vinaygupta.eventpipeline.pipeline.EventProcessor;
import in.vinaygupta.eventpipeline.store.EventStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "pipeline.broker-mode", havingValue = "kafka")
public class KafkaEventBroker implements EventBroker {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final PipelineProperties properties;
    private final EventStore store;
    private final EventProcessor processor;

    public KafkaEventBroker(KafkaTemplate<String, String> kafkaTemplate, PipelineProperties properties,
                            EventStore store, EventProcessor processor) {
        this.kafkaTemplate = kafkaTemplate;
        this.properties = properties;
        this.store = store;
        this.processor = processor;
    }

    @Override
    public void publish(String eventId) {
        store.update(eventId, event -> event.transition(EventStatus.PUBLISHED, "Published to Kafka", properties.topics().inventory()));
        kafkaTemplate.send(properties.topics().inventory(), eventId, eventId);
    }

    @Override
    public void publishRetry(String eventId) {
        kafkaTemplate.send(properties.topics().retry(), eventId, eventId);
    }

    @Override
    public void publishDeadLetter(String eventId) {
        kafkaTemplate.send(properties.topics().dlq(), eventId, eventId);
    }

    @KafkaListener(topics = "${pipeline.topics.inventory}", groupId = "event-pipeline-simulator")
    public void consumeInventory(String eventId) {
        processor.process(eventId);
    }

    @KafkaListener(topics = "${pipeline.topics.retry}", groupId = "event-pipeline-simulator")
    public void consumeRetry(String eventId) {
        processor.process(eventId);
    }

    @KafkaListener(topics = "${pipeline.topics.dlq}", groupId = "event-pipeline-simulator-dlq")
    public void consumeDeadLetter(String eventId) {
        store.update(eventId, event -> event.failed(EventStatus.DEAD_LETTERED,
                event.failureReason() == null ? "Consumed from DLQ" : event.failureReason(),
                properties.topics().dlq()));
    }
}
