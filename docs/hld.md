# High-Level Design

## System Context

```mermaid
flowchart LR
  User[Reviewer] --> Pages[GitHub Pages Dashboard]
  Pages --> API[Spring Boot API on Render or Local]
  API --> Broker{Broker Mode}
  Broker --> Kafka[Redpanda/Kafka Local]
  Broker --> Sim[Simulation Mode Hosted]
  API --> Store[In-memory Event Store]
```

## Components

- Dashboard: React/Vite static application hosted on GitHub Pages.
- API: Spring Boot REST service.
- Broker abstraction: common interface for Kafka and simulation modes.
- Worker: processes events, retries transient failures, routes failures to DLQ.
- Event store: in-memory recent event state for API and SSE.

## Deployment Topology

Local:

```mermaid
flowchart LR
  Browser --> Frontend[Nginx Static Frontend]
  Frontend --> Backend[Spring Boot]
  Backend --> Redpanda[Redpanda Kafka API]
```

Hosted:

```mermaid
flowchart LR
  Browser --> Pages[GitHub Pages]
  Pages --> Render[Render Spring Boot]
  Render --> Simulation[In-memory Simulation Broker]
```

## Runtime Modes

- `EVENT_BROKER_MODE=kafka`: publishes to Kafka topics and consumes with Spring Kafka listeners.
- `EVENT_BROKER_MODE=simulation`: publishes into an async in-memory worker flow.
