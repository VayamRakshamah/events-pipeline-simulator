# Requirements

## Problem Statement

Backend portfolios often claim event-driven architecture experience without showing a working event lifecycle. Event Pipeline Simulator turns that claim into a visible, runnable system.

## Users

- Recruiters and engineering managers reviewing backend depth.
- Backend engineers evaluating API, queueing, retry, and observability design.
- The portfolio owner recording short demo videos.

## Functional Requirements

- Publish an inventory event with `sku`, `storeId`, `quantityDelta`, and `scenario`.
- Process events through visible lifecycle states.
- Support success, validation failure, transient failure, poison message, and burst traffic scenarios.
- Show event metrics, recent events, topic path, retry count, latency, and failure reason.
- Expose a stable REST API and SSE stream for dashboard updates.
- Support Kafka locally and simulation mode for hosted demos.

## Non-Functional Requirements

- Local setup must run with one Docker Compose command.
- Hosted backend must not require paid Kafka for MVP.
- Frontend must be static-hostable on GitHub Pages.
- Backend must expose `/actuator/health` for Render.
- Code should be small, readable, and portfolio-review friendly.

## MVP Scope

- In-memory event state.
- Kafka-compatible local broker.
- Simulation mode on Render.
- React operations console.
- Enterprise documentation and CI/CD workflows.

## Out Of Scope

- Persistent database.
- Managed Kafka production deployment.
- Authentication.
- Multi-tenant event streams.
- Prometheus/Grafana integration.
