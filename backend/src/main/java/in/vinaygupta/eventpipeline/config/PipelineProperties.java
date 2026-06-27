package in.vinaygupta.eventpipeline.config;

import java.time.Duration;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pipeline")
public record PipelineProperties(
        String brokerMode,
        List<String> allowedOrigins,
        Topics topics,
        int maxRetries,
        Duration workerDelay
) {
    public record Topics(String inventory, String retry, String dlq) {
    }
}
