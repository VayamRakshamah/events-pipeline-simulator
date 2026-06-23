# Event Pipeline Simulator

A Spring Boot + Kafka backend lab with a product-style React dashboard for demonstrating event-driven architecture: publish, process, retry, dead-letter, observe.

The project has two runtime modes:

- `kafka`: local Docker Compose with Redpanda as the Kafka-compatible broker.
- `simulation`: hosted Render mode that preserves the same API and UI behavior without requiring managed Kafka.

## Live Deployment Shape

- Frontend: GitHub Pages.
- Backend: Render Web Service.
- Hosted backend env: `EVENT_BROKER_MODE=simulation`.
- Local backend env: `EVENT_BROKER_MODE=kafka`.

## What It Demonstrates

- Event publication and worker processing.
- Kafka topic path: `inventory.events`, `inventory.events.retry`, `inventory.events.dlq`.
- Retry and dead-letter behavior.
- Live operations dashboard with metrics and event detail timelines.
- Enterprise-style documentation: requirements, HLD, LLD, API contract, ADR, CI/CD notes.

## Run Locally

```bash
docker compose up --build
```

Open:

- Dashboard: `http://localhost:3001`
- Backend health: `http://localhost:8081/actuator/health`
- Metrics: `http://localhost:8081/api/metrics/pipeline`

## API Examples

```bash
curl -X POST http://localhost:8081/api/events/inventory \
  -H "Content-Type: application/json" \
  -d '{"sku":"SKU-4108","storeId":"BLR-1","quantityDelta":12,"scenario":"SUCCESS"}'

curl -X POST http://localhost:8081/api/scenarios/burst \
  -H "Content-Type: application/json" \
  -d '{"count":12}'

curl http://localhost:8081/api/events
curl http://localhost:8081/api/metrics/pipeline
```

## Documentation

- [Requirements](docs/requirements.md)
- [High-Level Design](docs/hld.md)
- [Low-Level Design](docs/lld.md)
- [API Contract](docs/api-contract.md)
- [Run Local](docs/run-local.md)
- [Deploy to GitHub Pages](docs/deploy-github-pages.md)
- [Deploy to Render](docs/deploy-render.md)
- [ADR 0001: Broker Mode](docs/adr/0001-broker-mode.md)
