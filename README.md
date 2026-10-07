# Parking Lot Management System

Spring Boot 4 / Java 21 backend for managing parking lots: lot administration,
vehicle entry/exit with spot allocation, hourly-fee tickets, and availability
queries. REST API (documented with OpenAPI), PostgreSQL persistence with
Flyway migrations.

## Prerequisites

- Java 21
- Docker (for the database and for Testcontainers in integration tests)
- That's it — Gradle comes via the wrapper (`./gradlew`)

## Quickstart

```bash
# 1. Start PostgreSQL (waits until the health check passes)
docker compose up -d --wait

# 2. Run the app (Flyway migrates the schema on startup)
./gradlew bootRun

# 3. Explore the API
open http://localhost:8080/swagger-ui.html
```

The application reads its database connection from environment variables,
falling back to the docker-compose defaults (`localhost:5433` — the compose
file maps host port 5433 because 5432 is often taken by other local
projects — db `parkinglot`, user `parking`, password `parking`):

| Variable                    | Default                                              |
|-----------------------------|------------------------------------------------------|
| `SPRING_DATASOURCE_URL`     | `jdbc:postgresql://localhost:5433/parkinglot`        |
| `SPRING_DATASOURCE_USERNAME`| `parking`                                            |
| `SPRING_DATASOURCE_PASSWORD`| `parking`                                            |

For a real deployment, set these to point at your managed database and keep
credentials out of the repo.

## Database

### Start / stop

```bash
docker compose up -d --wait   # start; --wait blocks until healthy
docker compose ps             # should show (healthy)
docker compose stop           # stop, data survives (named volume)
docker compose down           # remove container, data survives
docker compose down -v        # remove container AND wipe the data volume
```

### Migrations (Flyway)

The schema is owned by versioned SQL migrations in
`src/main/resources/db/migration`. Flyway runs them automatically on
application startup (and in integration tests); Hibernate runs with
`ddl-auto=validate`, so the entities must always match the migrated schema —
if they drift, startup fails loudly.

To change the schema, add a new file (never edit an applied migration):

```
src/main/resources/db/migration/V2__add_something.sql
```

Inspect the database directly:

```bash
docker compose exec postgres psql -U parking -d parkinglot
```

## Testing

```bash
./gradlew test        # everything: unit, MockMvc, Testcontainers integration
```

Integration tests spin up their own throwaway PostgreSQL via Testcontainers
(Docker must be running) and run the real Flyway migrations against it.

## API

Base path `/api/v1`:

| Method | Path                                     | Description                          |
|--------|------------------------------------------|--------------------------------------|
| POST   | `/parking-lots`                          | Create a lot with floors and spots   |
| GET    | `/parking-lots`                          | List lots                            |
| GET    | `/parking-lots/{id}`                     | Lot details incl. spot statuses      |
| GET    | `/parking-lots/{id}/availability`        | Currently free spots                 |
| POST   | `/parking-lots/{id}/vehicles/entry`      | Admit a vehicle, open a ticket       |
| POST   | `/tickets/{id}/exit`                     | Check out, compute and record fee    |
| GET    | `/tickets/{id}`                          | Ticket details                       |

Errors follow RFC 7807 (`application/problem+json`): 400 validation,
404 unknown lot/ticket, 409 lot full / ticket already completed.

## Project layout

```
src/main/java/.../pariking_lot_management/
├── api/                  # REST controllers, DTOs, exception advice
├── application/
│   ├── port/             # persistence interfaces owned by use cases
│   ├── usecase/          # entry / exit / parking-lot services
│   └── exception/
├── domain/               # framework-free model, enums, strategies
├── infrastructure/       # JPA entities, mappers, Spring Data, adapters
└── config/
```

The domain package has no Spring or JPA imports; the web and persistence
layers adapt to and from it.
