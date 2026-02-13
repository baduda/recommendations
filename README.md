# Crypto Recommendations Service

A service for analyzing cryptocurrency prices and providing investment recommendations based on volatility.

## Project Description
This service allows uploading historical cryptocurrency price data from CSV files, calculating key metrics (min, max, oldest, newest prices), and determining the most volatile assets for a given period or a specific day.

## Tech Stack
*   **Java 25**: Leveraging the latest language and JVM features.
*   **Spring Boot 4.0.2**: Modern framework for building microservices.
*   **PostgreSQL 17**: Reliable storage for price data.
*   **Virtual Threads (Project Loom)**: For high-performance parallel processing of import files.
*   **ShedLock**: Ensures that scheduled import tasks do not overlap in a clustered environment.
*   **Caffeine Cache**: Local caching of heavy query results.
*   **Bucket4j**: Rate Limiting implementation to protect the API from overloads.
*   **Flyway**: Database migrations.
*   **MapStruct**: High-performance mapping between entities and DTOs.

## Architecture
The project is implemented using **Hexagonal Architecture (Ports & Adapters)**.

### Package Structure:
*   `domain`: The core of the system. Contains business logic, domain services (`CryptoAnalysisService`), and entities. Independent of external frameworks.
*   `application`: Service layer (`CryptoApplicationService`), orchestrating business scenarios.
*   `infrastructure`: Implementation of "adapters" — database interaction, configurations (ShedLock, Caching), error handling.
*   `interfaces`: System entry points (REST controllers).

### Why is this better than the Layered approach?
1.  **Technology Independence**: Business logic doesn't know whether we use PostgreSQL or MongoDB, REST or gRPC.
2.  **Testability**: Domain logic is easy to test with unit tests without starting the Spring context.
3.  **Flexibility**: Easy to replace implementation details (e.g., import method) without affecting the system core.

## Getting Started

### Prerequisites
*   Docker & Docker Compose
*   JDK 25
* 
### Data Setup
Before starting, ensure your CSV data is located in the `./prices` directory.
The application will scan this folder on startup and then once a day according to the configured cron schedule.

### Running Locally via Gradle
1. Ensure PostgreSQL is running (you can use `docker-compose up db`).
2. Start the application:
   ```bash
   ./gradlew bootRun
   ```

### Running via Docker Compose
Build and run the entire environment with a single command:
```bash
docker-compose up --build
```
The service will be available at: `http://localhost:8080`
Swagger UI: `http://localhost:8080/swagger-ui.html`

## Features
*   **Import**: Automatic discovery and import of CSV files from a configured directory at startup and on a schedule (once a day).
*   **Stats**: Get full statistics (min, max, oldest, newest) for a specific cryptocurrency.
*   **Sorting**: A list of all currencies sorted by volatility (normalized range).
*   **Highest Range**: Find the most volatile currency for a specific day.
*   **Rate Limiting**: Limit the number of API requests (configurable in `application.yaml`).

## Performance & Scalability
The service is designed to handle increased load and 100+ new cryptocurrencies:
1.  **Batch Loading**: CSV data is loaded in batches (`batch-size: 1000`), minimizing database queries.
2.  **Virtual Threads**: Each import file is processed in a separate virtual thread, allowing efficient CPU usage without overloading the OS with heavy threads.
3.  **Indexes**: The `crypto_prices` table has a composite index `(symbol, price_timestamp DESC)` for instant search and sorting.
4.  **Caching**: Volatility calculation results are cached via Caffeine, reducing DB load for repeated requests.

## Shortcuts & Trade-offs
*   **In-Memory Rate Limiting**: Local `Bucket4j` is used. In a distributed environment (K8s), a transition to a Redis-backed solution may be required.
*   **CSV Validation**: Basic format checks are performed. It is assumed that the input data is generally correct.
*   **No Auth**: For demonstration purposes, the API is open without authentication.
*   **Distributed Locks**: ShedLock is configured with a JDBC provider. This ensures that in a multi-instance environment (like Kubernetes), the import task runs only on one instance at a time.
*   **Batch Ingestion**: To ensure performance, the system uses JdbcTemplate for batch inserts, which is significantly faster than standard JPA save calls for large datasets.

## Testing
To run tests, use:
```bash
./gradlew test
```
The project uses **Testcontainers (PostgreSQL)** for integration tests, guaranteeing identical behavior in tests and production.

### JaCoCo Report
After running the tests, the coverage report is available at:
`build/reports/jacoco/test/html/index.html`
Minimum coverage threshold is **80%**.

## Monitoring & Health
* **Health Checks**: Available at `/actuator/health` (Liveness/Readiness for K8s).
* **Metrics**: Prometheus-ready metrics available at `/actuator/prometheus`.
* **Logging**: Structured JSON logging is enabled for better integration with ELK/Loki.
