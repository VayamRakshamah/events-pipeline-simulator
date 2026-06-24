package in.vinaygupta.eventpipeline.domain;

public enum EventStatus {
    RECEIVED,
    PUBLISHED,
    VALIDATING,
    PROCESSING,
    COMPLETED,
    RETRYING,
    FAILED,
    DEAD_LETTERED
}
