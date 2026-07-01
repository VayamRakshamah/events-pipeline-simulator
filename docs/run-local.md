# Run Local

## Docker Compose

```bash
docker compose up --build
```

Services:

- Redpanda Kafka API: `localhost:19092`
- Backend: `http://localhost:8081`
- Frontend: `http://localhost:3001`

## Demo Script

1. Open `http://localhost:3001`.
2. Publish a `SUCCESS` event.
3. Publish a `TRANSIENT_FAILURE` event and watch retry behavior.
4. Publish a `POISON_MESSAGE` event and watch DLQ behavior.
5. Click `Burst Traffic`.
6. Review metrics, topic path, and selected event timeline.

## API Smoke Test

```bash
curl -s http://localhost:8081/actuator/health
curl -s http://localhost:8081/api/metrics/pipeline
```
