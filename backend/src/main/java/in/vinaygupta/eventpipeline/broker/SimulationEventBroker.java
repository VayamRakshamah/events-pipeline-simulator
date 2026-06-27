package in.vinaygupta.eventpipeline.broker;

import in.vinaygupta.eventpipeline.config.PipelineProperties;
import in.vinaygupta.eventpipeline.pipeline.EventProcessor;
import in.vinaygupta.eventpipeline.store.EventStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "pipeline.broker-mode", havingValue = "simulation", matchIfMissing = true)
public class SimulationEventBroker implements EventBroker {
    private final EventStore store;
    private final EventProcessor processor;
    private final PipelineProperties properties;
    private final ThreadPoolTaskExecutor executor;

    public SimulationEventBroker(EventStore store, EventProcessor processor, PipelineProperties properties) {
        this.store = store;
        this.processor = processor;
        this.properties = properties;
        this.executor = new ThreadPoolTaskExecutor();
        this.executor.setThreadNamePrefix("pipeline-sim-");
        this.executor.setCorePoolSize(3);
        this.executor.setMaxPoolSize(6);
        this.executor.initialize();
    }

    @Override
    public void publish(String eventId) {
        store.update(eventId, event -> event.transition(
                in.vinaygupta.eventpipeline.domain.EventStatus.PUBLISHED,
                "Published to inventory topic",
                properties.topics().inventory()));
        executor.execute(() -> processor.process(eventId));
    }

    @Override
    public void publishRetry(String eventId) {
        executor.execute(() -> processor.process(eventId));
    }

    @Override
    public void publishDeadLetter(String eventId) {
        store.update(eventId, event -> event.failed(
                in.vinaygupta.eventpipeline.domain.EventStatus.DEAD_LETTERED,
                event.failureReason() == null ? "Moved to DLQ" : event.failureReason(),
                properties.topics().dlq()));
    }
}
