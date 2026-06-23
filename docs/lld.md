# Low-Level Design

## Package Design

- `api`: controllers, request DTOs, response DTOs, error handler.
- `domain`: event status, event scenario, event aggregate, timeline entry.
- `pipeline`: application service and event processor.
- `broker`: broker interface plus Kafka and simulation implementations.
- `metrics`: pipeline metrics snapshot.
- `store`: in-memory event store.
- `config`: app configuration, CORS, broker settings.

## State Machine

```mermaid
stateDiagram-v2
  [*] --> RECEIVED
  RECEIVED --> PUBLISHED
  PUBLISHED --> VALIDATING
  VALIDATING --> PROCESSING
  VALIDATING --> DEAD_LETTERED: validation failure
  PROCESSING --> COMPLETED: success
  PROCESSING --> RETRYING: transient failure
  RETRYING --> VALIDATING
  PROCESSING --> DEAD_LETTERED: poison message
```

## Retry And DLQ Rules

- `SUCCESS`: completes on first processing attempt.
- `VALIDATION_FAILURE`: moves directly to `DEAD_LETTERED`.
- `TRANSIENT_FAILURE`: moves to `RETRYING` until `maxRetries`, then completes.
- `POISON_MESSAGE`: moves to `DEAD_LETTERED`.

## Sequence

```mermaid
sequenceDiagram
  participant UI as Dashboard
  participant API as Spring Boot API
  participant Broker as EventBroker
  participant Worker as EventProcessor
  participant Store as EventStore

  UI->>API: POST /api/events/inventory
  API->>Store: save RECEIVED
  API->>Broker: publish(eventId)
  Broker->>Store: mark PUBLISHED
  Broker->>Worker: process(eventId)
  Worker->>Store: VALIDATING -> PROCESSING -> terminal
  UI->>API: GET /api/events
```
