# ADR 0001: Kafka Locally, Simulation On Render

## Status

Accepted

## Context

The project should showcase Kafka-style event-driven architecture while remaining cheap and reliable for a public portfolio demo.

## Decision

Use a broker abstraction:

- Local mode uses Kafka-compatible Redpanda.
- Hosted Render mode uses simulation.
- Managed Kafka can be added later without changing the dashboard contract.

## Consequences

- The portfolio demo remains deployable without paid infrastructure.
- Local mode still demonstrates real Kafka topics and consumers.
- The hosted demo is honest about being simulation-backed.
