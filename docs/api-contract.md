# API Contract

Base URL: `/api`

## Publish Inventory Event

```http
POST /events/inventory
Content-Type: application/json
```

```json
{
  "sku": "SKU-4108",
  "storeId": "BLR-1",
  "quantityDelta": 12,
  "scenario": "SUCCESS"
}
```

Scenarios:

- `SUCCESS`
- `VALIDATION_FAILURE`
- `TRANSIENT_FAILURE`
- `POISON_MESSAGE`

## Burst Traffic

```http
POST /scenarios/burst
Content-Type: application/json
```

```json
{ "count": 12 }
```

## List Events

```http
GET /events
```

## Event Detail

```http
GET /events/{eventId}
```

## Metrics

```http
GET /metrics/pipeline
```

```json
{
  "totalEvents": 12,
  "completedEvents": 7,
  "retriedEvents": 3,
  "deadLetteredEvents": 4,
  "successRate": 0.58,
  "averageLatencyMs": 1430
}
```

## SSE Stream

```http
GET /stream/events
Accept: text/event-stream
```

The stream emits recent event lists under event name `events`.
