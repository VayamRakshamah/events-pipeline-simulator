package in.vinaygupta.eventpipeline.broker;

public interface EventBroker {
    void publish(String eventId);

    void publishRetry(String eventId);

    void publishDeadLetter(String eventId);
}
