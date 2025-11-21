# Big Data Kafka Assignment

Kafka-based mini platform that publishes and aggregates `OrderEvent` messages serialized with Avro. The producer exposes HTTP endpoints for sending single or bulk orders (including auto-generated random payloads). The consumer listens to the main and retry topics, maintains running averages per product and globally, and pushes permanently failing records to a DLQ.

## Features
- **Avro everywhere**: shared `OrderEvent` schema published through Confluent Schema Registry.
- **RESTful producer**: `/api/orders` endpoints for single orders, manual batches, and `count`-driven random batches.
- **Randomized traffic generator**: quickly seed Kafka with realistic pricing via `/api/orders/random` or `count` batches.
- **Resilient consumer**: retry topic (`orders.v1.retry`) with 3 attempts before publishing to `orders.v1.dlq`.
- **Real-time analytics**: consumer logs per-product and global running averages using thread-safe aggregators.
- **Docker-first**: single `docker-compose.yml` spins up Kafka, Schema Registry, Kafka UI, and both Spring Boot services.

## Repository Layout
```
producer/            # Spring Boot producer with REST API
consumer/            # Spring Boot consumer with Kafka listener + aggregation
pom.xml              # Parent Maven aggregator
Dockerfile           # Service-specific Dockerfiles (Chainguard base images)
docker-compose.yml   # Full environment
```

## Prerequisites
- Java 17+
- Maven 3.9+
- Docker Desktop 24+ with Compose V2

## Quick Start (Docker)
1. **Clean previous stack (optional but recommended)**
   ```powershell
   docker compose down -v
   ```
2. **Build fresh images and start everything**
   ```powershell
   docker compose up --build -d
   ```
3. **Verify infrastructure**
   - Kafka UI at <http://localhost:8080>
   - Schema Registry at <http://localhost:8081/subjects>
4. **Check topics** – you should see:
   - `orders.v1`
   - `orders.v1.retry`
   - `orders.v1.dlq`

## Local Development (without Docker)
```powershell
# Start ZooKeeper/Kafka/Schema Registry via docker compose (optional)
docker compose up -d zookeeper kafka schema-registry kafka-ui

# Run producer locally on :8080
cd producer
mvn spring-boot:run

# In another terminal run consumer on :8081
cd consumer
mvn spring-boot:run
```
Make sure the `KAFKA_BOOTSTRAP_SERVERS` and `SCHEMA_REGISTRY_URL` env vars (or defaults in `application.yml`) point to your running cluster.

## Environment Variables
| Variable | Default | Used By | Description |
| --- | --- | --- | --- |
| `KAFKA_BOOTSTRAP_SERVERS` | `kafka:9092` (Docker) / `localhost:29092` (local) | Producer & Consumer | Kafka broker endpoints |
| `SCHEMA_REGISTRY_URL` | `http://schema-registry:8081` | Producer & Consumer | Confluent Schema Registry URL |
| `ORDER_TOPIC` | `orders.v1` | Both | Main topic for `OrderEvent`s |
| `ORDER_RETRY_TOPIC` | `orders.v1.retry` | Consumer | Intermediate retry topic before DLQ |
| `ORDER_DLQ_TOPIC` | `orders.v1.dlq` | Consumer | Dead-letter queue topic |
| `SERVER_PORT` | Producer `8080`, Consumer `8081` | Each service | HTTP port exposed by Spring Boot |

## REST API (Producer)
Base URL depends on how you run the service:
- Docker: `http://localhost:8082`
- Local Spring Boot: `http://localhost:8080`

| Method | Path | Description |
| --- | --- | --- |
| `POST` | `/api/orders` | Publish a single order from request body |
| `POST` | `/api/orders/random?product=Phone` | Publish one random order (optional product override) |
| `POST` | `/api/orders/batch` | Publish a list of explicit orders (JSON body) |
| `POST` | `/api/orders/batch?count=100&product=Laptop` | Auto-generate and publish `count` random orders |

### Payloads & Responses
**Single order request**
```json
{
  "orderId": "O-1001",
  "product": "Keyboard",
  "price": 49.99
}
```

**Batch request**
```json
{
  "orders": [
    { "orderId": "O-2001", "product": "Laptop", "price": 1299.95 },
    { "orderId": "O-2002", "product": "Mouse", "price": 29.50 }
  ]
}
```

**Response** (all endpoints)
```json
{
  "status": "accepted",
  "processed": 2
}
```
`processed` equals the number of orders handed off to Kafka.

## Consumer Behavior
- Subscribes to both `orders.v1` and `orders.v1.retry` via a single `@KafkaListener`.
- Common error handler retries each failure twice (3 total attempts) with a 2-second backoff.
- After the final attempt, records move to `orders.v1.dlq` via `DeadLetterPublishingRecoverer`.
- Logs look like: `Product avg: 210.52 (count 5), Global avg: 175.11 (count 12)` to show aggregation results.

## Testing & Verification
```powershell
# Unit tests
mvn test

# Targeted module tests
mvn -pl producer test
mvn -pl consumer test
```
You can also tail the Docker logs to observe message flow:
```powershell
docker logs -f producer-service
docker logs -f consumer-service
```

## Troubleshooting
- **Image rebuilds downloading dependencies**: the Chainguard Maven image caches the Maven repo layer, so repeated `docker compose up --build` runs only fetch changes.
- **Topics missing**: ensure `consumer-service` has started; it auto-creates the main/retry/DLQ topics through Spring Kafka `NewTopic` beans.
- **404 on new endpoints**: rebuild the producer image (`docker compose build producer-service`) so the latest controller code is packaged.

## Next Steps
- Expand observability (Prometheus/Grafana) for throughput and latency metrics.
- Add contract tests for API validation and Avro schema compatibility checks.
- Implement automated load generators that hit the random endpoints on a schedule.
