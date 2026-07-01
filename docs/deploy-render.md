# Deploy Backend To Render

Deploy `backend/` as a Render Web Service.

## Runtime

The Spring Boot app binds to:

```text
server.address=0.0.0.0
server.port=${PORT}
```

## Environment Variables

```text
EVENT_BROKER_MODE=simulation
ALLOWED_ORIGINS=https://vinaygupta.in
PIPELINE_MAX_RETRIES=2
PIPELINE_WORKER_DELAY=450ms
```

## Health Check

```text
/actuator/health
```

## Notes

Render hosts the Spring Boot API. It does not provide a managed Kafka broker for this MVP, so the public hosted demo uses simulation mode. Local development uses Redpanda/Kafka through Docker Compose.
