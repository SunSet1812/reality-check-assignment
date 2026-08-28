# Reality Check service

A small backend service for our **Reality Check** responsible-gaming feature.

While a player is in a gaming session, the service periodically produces a *reality check*:
a reminder of how long they have been playing and their net win/loss so far. The player is
expected to acknowledge the reminder and decide whether to keep playing or stop. The service
keeps, per player, the state of the current reality-check session (interval, elapsed time,
net amount, whether it has been acknowledged, and when the next check is due).

This repository started as a legacy Reality Check application. The service was functional,
but had accumulated technical debt and needed improvements to make it more maintainable,
scalable, reliable, and easier to use.

## Tech stack

- Java 25
- Spring Boot 4.1
- Spring Web + Spring Data JPA
- H2 in-memory database, started in **MySQL compatibility mode**
- Liquibase (schema + seed data applied automatically on startup)
- springdoc-openapi / Swagger UI
- Redis
- Spring Scheduler
- ShedLock with Redis as the distributed lock provider
- Maven

## Running it

### With Docker (recommended)

```bash
docker compose up --build
```

### Locally with Maven

Requires JDK 25.

```bash
mvn clean package
java -jar target/reality-check.jar
```

### With Redis

Redis is started automatically when using Docker Compose.

If connecting through Redis Insight:

```text
Host: localhost
Port: 6379
```

The service starts on port `8080` under the context path `/reality-check`.
Redis is available on port `6379`.
On startup Liquibase creates the required database tables and seeds a few rows.

## API documentation

- Swagger UI: http://localhost:8080/reality-check/api-docs/swagger-ui
- OpenAPI JSON: http://localhost:8080/reality-check/v3/api-docs
- H2 console: http://localhost:8080/reality-check/h2-console
  (JDBC URL `jdbc:h2:mem:realitycheck`, user `sa`, empty password)

The API documentation includes endpoint summaries, descriptions, request/response information,
and the main processing flow for the player session operations.

## Seeded data

| Player ID | Franchise | Reality-check session |
|-----------|-----------|-----------------------|
| 1001      | 10        | ACTIVE, 60 min interval, acknowledged |
| 1002      | 10        | ACTIVE, 30 min interval, not acknowledged |
| 1003      | 20        | none yet |

## Example requests

```bash
BASE=http://localhost:8080/reality-check

# Current reality-check status for a player
curl "$BASE/getStatus/1001"

# Get the current reality check, or start one if none exists
curl "$BASE/getOrStartCheck/1003/45"

# Acknowledge the current reality check
curl -X PUT "$BASE/acknowledge/1002"
```

---

## Assignment

This service was functional but had grown organically and accumulated technical debt. The
following requests were provided as part of the assignment. The implementation below describes
how each requirement was addressed.

### 1. Engineering — pay down the debt

The legacy persistence and application structure were refactored to improve maintainability,
separation of concerns, loose coupling, and correctness.

- Replaced **JDBI with Spring Data JPA** for database persistence.
- Introduced a layered architecture:

```text
Controller
    ↓
   DTO
    ↓
 Service
    ↓
   DAO
    ↓
Repository
    ↓
 Database
```

- Added separate **Controller, Service, DAO, and Repository** layers to separate responsibilities.
- Introduced **DTOs** so API request/response models are separated from persistence entities.
- Fixed controller request mappings to use the appropriate HTTP methods.
- Improved naming and structure where appropriate.

### 2. QA — make the API usable and documented

The API documentation was improved so that QA can understand the available endpoints without
having to ask developers how they work.

- Added **Swagger/OpenAPI documentation** for the REST API.
- Added meaningful **endpoint summaries and descriptions**.
- Documented the main processing **flow** for player session operations.
- Added request/response documentation to make the endpoints easier to understand and test.

### 3. Tech Ops — scale out

The service was made safer to run with multiple application instances.

- Used **transactional boundaries** for session read/update operations to maintain consistency
  when multiple requests or application instances operate concurrently.
- Used **ShedLock** for scheduled tasks so that when multiple application instances are running,
  the scheduled task is executed by only **one instance at a time**.
- Used **Redis as the shared distributed lock provider** for ShedLock, allowing all application
  instances to coordinate the scheduler lock through the same shared store.
- Redis is therefore external to the application instance, allowing multiple application
  instances to share the same scheduler-lock state.

### 4. Compliance — persist acknowledgement timestamps

Compliance requires every acknowledgement to be persisted with its exact date and time.

- Added a separate **`SessionAcknowledgment`** entity containing an **`Instant`** timestamp.
- Every time a player acknowledges a reality check, a separate acknowledgement record is
  persisted in the database.
- This allows a single continuous player session to have **multiple acknowledgements** without
  creating a new session for every acknowledgement.

The resulting relationship is conceptually:

```text
Player
  ↓
Active Session
  ↓
Multiple SessionAcknowledgments
  ├── acknowledgement timestamp
  ├── acknowledgement timestamp
  └── acknowledgement timestamp
```

### 5. Frontend (React) — return a formatted timestamp

The timestamp representation was changed to support timezone-aware responses.

- Changed relevant timestamp fields from **`long` to `Instant`**.
- `Instant` provides a timezone-independent point in time that can be converted into the
  player's local timezone.
- Added a **mapper** that converts the `Instant` from the entity into the response DTO.
- The mapper uses the player's `timezone` value to convert the timestamp to the player's
  local time.
- The response is formatted according to the requested format:

```text
6 July 26 14:35
```

No timezone suffix is included in the formatted response.

---

## Architecture

The service follows a layered architecture to keep responsibilities separated and reduce
coupling between the API, business logic, and persistence layers.

```text
                    Client
                      ↓
                  Controller
                      ↓
                     DTO
                      ↓
                   Service
                      ↓
                     DAO
                      ↓
                 Repository
                      ↓
                   Database
```

For scheduled processing, the application instances coordinate through Redis:

```text
             Kubernetes / Multiple Instances

             ┌───────────────┐
             │   App Pod 1   │
             └───────┬───────┘
                     │
             ┌───────┴───────┐
             │     Redis     │
             │   ShedLock    │
             └───────┬───────┘
                     │
             ┌───────┴───────┐
             │   App Pod 2   │
             └───────────────┘

             Only one instance
             executes the scheduled task
             at a time.
```

## Notes

- H2 remains an in-memory database for the self-contained assignment environment.
- Redis is used for caching and to store shared distributed scheduler locks.
- Liquibase manages database schema changes and seed data.
- The service can run as a single instance or as multiple application instances.