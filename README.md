# Webstore Order Management REST API

A course project: a small order management system / webshop back-end service
built on a REST API, backed by the provided `webstore` MariaDB database
(Spring Boot 3.5, Spring Data JPA, MariaDB).

API design follows the **webshop view for end users (customers)** — browsing
products, placing orders, managing one's own orders, addresses and contact
information — with a complementary admin view to provide full CRUD on
catalogue resources. The work plan is in [PROJECT_PLAN.md](PROJECT_PLAN.md).

## Status

Work in progress (scaffold completed). As features are implemented, full
endpoint documentation (purpose, request and response formats) and the
database internals not visible through the API (indexes, views, triggers,
transactions, temporal features, security) will be documented here.

### Implemented so far

| Endpoint | Method | Description |
|---|---|---|
| `/products/{id}` | GET | Read one product by id, 404 if missing |

## Requirements

- Java 21
- Maven
- MariaDB with the provided `webstore` schema loaded (see `Project Data/`)
- An application database user created per
  [grants.sql](webstore-api/src/main/resources/db/grants.sql) — the app
  connects with **least privileges**, never as root/admin

## Running

```bash
cd webstore-api

# least-privilege app user's password (not stored in the repository)
export WEBSTORE_DB_PASSWORD='<your password>'
mvn spring-boot:run
```

Then:

- `GET http://localhost:8080/products/{id}`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI spec: `http://localhost:8080/v3/api-docs`

## Database setup (DBA, once)

Run as an admin account, not as the application user:

```bash
mariadb -h 127.0.0.1 -u root -p < webstore-api/src/main/resources/db/grants.sql
```

Passwords are never committed to this repository; the application reads
`WEBSTORE_DB_PASSWORD` / `WEBSTORE_DB_USER` (default: `webstore_app`) from
the environment. See [PROJECT_PLAN.md §6](PROJECT_PLAN.md#6-database-features-views-triggers-indexing-temporal-security-backups)
for the plan on views, triggers, indexing, transactions, temporal features,
security and backups.
