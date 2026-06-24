package in.vinaygupta.eventpipeline.domain;

public enum EventScenario {
    SUCCESS,
    VALIDATION_FAILURE,
    TRANSIENT_FAILURE,
    POISON_MESSAGE
}
