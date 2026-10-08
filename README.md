# Webstore Order Management REST API

A course project: a small order management system / webshop back-end service
built on a REST API, backed by the provided `webstore` MariaDB database
(Spring Boot 3.5, Spring Data JPA, MariaDB).

API design follows the **webshop view for end users (customers)** — browsing
products, placing orders, managing one's own orders, addresses and contact
information — with a complementary admin view to provide full CRUD on
catalogue resources. The work plan is in [PROJECT_PLAN.md](PROJECT_PLAN.md).

## Status

All five work-order steps are done: scaffold, domain layer, CRUD + query
endpoints, database features, and this documentation. Everything below is
implemented and verified against the populated course database; the plan and
per-step verification evidence live in [PROJECT_PLAN.md](PROJECT_PLAN.md).

### Implemented (step 2) — domain mapping highlights

- All 9 webstore tables mapped to entities; associations: 1:M
  (customer→addresses, supplier→addresses/products, category→products,
  order→items), **N:M Orders ↔ Products** through `orderitems` (explicit
  entity, composite PK `(order_id, product_id)`, carrying quantity/unit-price),
  1:1 `Order ↔ shipping address` via `orders.shipping_address_id`
- **JOINED inheritance** `Product → PhysicalProduct / DigitalProduct`
  (discriminator `products.dtype`; documented schema change
  `db/product_subtypes.sql`; base products = `P`)
- **Loading choices**: category/supplier ManyToOne = EAGER (always-relevant
  small lookups); all collections and the order's customer/product links =
  LAZY, loaded by dedicated JOIN FETCH queries
- `OrderStatus` enum ↔ varchar via auto-apply `AttributeConverter` (unknown DB
  values fail loudly); `ProductListener` JPA entity listener (`@PostPersist`,
  `@PreUpdate` audit logging)

### Implemented (step 3)

- **Order aggregate, transactional** (plan §4 #13–#15, #21; §5): checkout
  inserts the order + items and decrements stock under `SELECT ... FOR UPDATE`
  pessimistic locks (409 on insufficient stock); cart editing (add / change
  quantity / remove) re-syncs stock in the same transaction; status lifecycle
  `NEW → SHIPPED → DELIVERED` and `NEW → CANCELLED` (cancel returns the
  reserved stock); only `CANCELLED` orders can be deleted.
- **Queries combining tables** (plan §4 #1–#12): catalogue search with
  filters, pagination and sorting; category statistics with GROUP BY + HAVING;
  customer detail combining `customers` + `contacts` + `customeraddresses` +
  `orders`/`orderitems`; customers without orders (NOT EXISTS subquery);
  dynamic customer search (Specification/Criteria); per-customer order history
  and order detail (JOIN FETCH for N+1 avoidance, per-item subtotals).
- **Admin CRUD**: products incl. `PHYS`/`DIG` subclasses (C/U, no delete per
  plan §2), product categories (full CRUD), suppliers + supplier addresses
  (full CRUD), customer addresses (C-R-U-D) and customer contacts (C-R-U).
- **Optimistic locking** (plan §5): `@Version` on `Product`
  ([db/optimistic_locking.sql](webstore-api/src/main/resources/db/optimistic_locking.sql));
  a stale `version` on the price update returns 409.
- Central error handling: RFC 7807 ProblemDetail responses for 404 / 400 / 409.

### Implemented (step 4) — database features

Not all of these are visible through the API; they are documented here in
full. Verification evidence per feature is noted; scripts live in
[db/](webstore-api/src/main/resources/db/).

- **Views** (SELECT-granted read-only views, `db/views.sql`), **serving API
  responses** (verified live):
  - `order_totals` — one row per order: customer name, item count, total
    amount (aggregates `orders`+`orderitems`); serves `GET /orders`,
    `GET /orders/search` and `GET /orders/customer/{id}` (one page query +
    one count query against the view, newest first)
  - `customer_summary` — per-customer order count, non-cancelled total spent,
    latest order date; serves the customer detail aggregate fields
    (`GET /customers/{id}`) and `GET /customers/top-spenders` (ranked by
    total spent; email joined back from `customers`)
  - *Semantic note:* the view's `total_spent` excludes CANCELLED orders —
    customer detail and top-spenders therefore report spend from live orders
- **Triggers** (`db/triggers.sql`), write with DEFINER rights so the API user
  needs **no** write grants on the tables (least privilege preserved, verified
  live):
  - `products` price change → `productpricehistory` (only on real change;
    also on insert, incl. the seed of the 1 000 existing products)
  - `orders` status change → `orderstatuslog` (old→new status + ms timestamp)
- **Temporal feature** (MariaDB system versioning, ≥ 10.3), **exposed and
  verified live through the API**:
  - `GET /products/{id}/price-history` — full change log, oldest first
    (price, `changedAt`, `validUntil`; `validUntil` far-future for the
    current version)
  - `GET /products/{id}/price?at=` — price at time T via
    `FOR SYSTEM_TIME AS OF T`; without `at` → current price. If no version
    existed at T (typically T before this feature was deployed), the
    documented fallback returns the current `products.price` and the response
    carries it with `source=CURRENT_FALLBACK` (vs `HISTORICAL` / `CURRENT`)
  - verified live: changing product 1's price via
    `PUT /products/{id}/price` appended the old version to the history and
    the "price as of the previous minute" query returned the pre-change price
- **Scheduled event** (`db/events.sql`,
  `ev_daily_sales_yesterday`, runs nightly at 03:00, scheduler enabled):
  refreshes `dailysales` (per sale date: order count, non-cancelled total).
  Logic verified by manual run for 2024-04-03 → 205 orders, €1 584 385.70
  (matches the 290 orders that day minus 85 CANCELLED).
- **Indexing** (`db/indexes.sql`, measurements in
  [docs/index_plan.md](webstore-api/docs/index_plan.md)): 4 new indexes
  (the status-log table's own index is created with it in `triggers.sql`);
  `EXPLAIN` before/after (e.g. customer email lookup
  99 688 rows examined → 1; status+date COUNT 199 806 → 6 010 with a covering
  index; FK-join control query unchanged). Indexes exist only where a measured
  access pattern justifies the write cost, reasoned in docs (relevant for the
  §5 transactional checkout).

## Endpoints

An interactive description of every endpoint is also served live by
Swagger/OpenAPI ([Running](#running) below); this section is the complete
reference.

List endpoints accept `page` and `size` (default 20; `size` is capped at 100
via Spring's pageable resolver) and respond with
the stable envelope `{content, page, size, totalElements, totalPages}`.
`sort` is honored where noted; the order listings are fixed to
newest-first (page/size only), because they are served by the
`order_totals` view.

### Catalogue & products

| Method | Endpoint | Purpose | Request | Response |
|---|---|---|---|---|
| GET | `/products` | Catalogue listing; filters `category`, `supplier`, `minPrice`, `maxPrice`, `search` (name contains), paginated + sorted | query params | page of ProductDto |
| GET | `/products/{id}` | Product detail incl. subclass attributes (`type`, `weightGrams`/`downloadUrl`) and current `version` | – | ProductDetailDto |
| GET | `/products/{id}/price-history` | Price change log, oldest first — temporal feature, system-versioned history | – | list of PriceHistoryEntryDto |
| GET | `/products/{id}/price` | Price at time T (`?at=` ISO date-time); without `at` → current price; pre-deployment times fall back to the current price with `source=CURRENT_FALLBACK` — temporal feature | `?at=` | ProductPriceAtDto |
| GET | `/products/price/{min}` | Products with price ≥ min (derived query) | – | list of ProductDto |
| GET | `/products/category/{id}` | Products of one category together with the category's own data | – | CategoryProductsDto |
| GET | `/products/stats/by-category` | Per-category count / avg / min / max price; `minProducts` restricts via HAVING | `?minProducts=` | list of CategoryStatsDto |
| POST | `/products` | Admin create; `type` = `P` (default) / `PHYS` / `DIG`, subclass fields `weightGrams`, `downloadUrl` | ProductWriteDto | 201 ProductDetailDto |
| PUT | `/products/{id}` | Admin update (type immutable → 400) | ProductWriteDto | ProductDetailDto |
| PUT | `/products/{id}/price` | Price change; optional `version` → 409 if stale | `{price, version?}` | ProductDetailDto |
| PUT | `/products/categories/{id}/price-bulk` | Bulk price change for every product of one category, one `@Modifying` UPDATE statement (versions bumped, history trigger still records it); 404 unknown category, 400 bad factor | `?factor=` (e.g. 1.1 = +10 %) | `{"updated N products..."}` |

### Product categories (admin, plan §4 #18)

| Method | Endpoint | Purpose | Request | Response |
|---|---|---|---|---|
| GET | `/products/categories` | List all categories | – | list of ProductCategoryDto |
| GET | `/products/categories/{id}` | Read one category | – | ProductCategoryDto |
| GET | `/products/categories/{id}/products` | Paged products of the category | query params | page of ProductDto |
| POST | `/products/categories` | Create | `{name, description?}` | 201 ProductCategoryDto |
| PUT | `/products/categories/{id}` | Update | `{name, description?}` | ProductCategoryDto |
| DELETE | `/products/categories/{id}` | Delete; 409 while products still reference it | – | 204 |

### Customers

| Method | Endpoint | Purpose | Request | Response |
|---|---|---|---|---|
| GET | `/customers` | Customer list; `search` matches first/last name or email | query params | page of CustomerDto |
| GET | `/customers/{id}` | Detail combining contact, addresses and order history (order count, total spent, latest order) | – | CustomerDetailDto |
| GET | `/customers/without-orders` | Customers missing from `orders` (NOT EXISTS subquery) | query params | page of CustomerDto |
| GET | `/customers/top-spenders` | Customers ranked by summed line totals; `limit` (default 10) | `?limit=` | list of TopSpenderDto |
| GET | `/customers/{id}/contact` | Read the contact matched by the customer's email; 404 if none | – | ContactDto |
| POST | `/customers/{id}/contact` | Create a contact with the customer's email + generated 32-char reference; 409 if one already matches | – | 201 ContactDto |
| PUT | `/contact/{id}` | Change a contact's email (reference immutable) | `{email}` | ContactDto |
| GET | `/customers/{id}/addresses` | List a customer's delivery addresses | – | list of CustomerAddressDto |
| POST | `/customers/{id}/addresses` | Create a delivery address | `{streetAddress, postalCode?, city, country?}` | 201 CustomerAddressDto |
| GET | `/customeraddresses/{id}` | Read one address | – | CustomerAddressDto |
| PUT | `/customeraddresses/{id}` | Update an address | `{streetAddress, postalCode?, city, country?}` | CustomerAddressDto |
| DELETE | `/customeraddresses/{id}` | Delete; 409 if an order references it as shipping address | – | 204 |

### Orders

| Method | Endpoint | Purpose | Request | Response |
|---|---|---|---|---|
| GET | `/orders` | Order list; filters `status`, `customerId`; `page`/`size` honored, newest first (fixed order). Served by the `order_totals` view: customer name + item count + total amount come from the view | query params | page of OrderSummaryDto |
| GET | `/orders/search` | Dynamic multi-criteria search over the `order_totals` view: `status`, `customerId`, `dateFrom`, `dateTo` (ISO date-time) | query params | page of OrderSummaryDto |
| GET | `/orders/customer/{customerId}` | A customer's order history (view-backed), optional `status` | query params | page of OrderSummaryDto |
| GET | `/orders/{id}` | Order detail: items with product names + subtotals, order total, shipping address | – | OrderDetailDto |
| POST | `/orders` | Checkout: transactional, pessimistic stock locking; 409 on insufficient stock, unknown product/address | OrderCreateRequest | 201 OrderDetailDto |
| PUT | `/orders/{id}/status` | Status transition (rules below) | `{status}` | OrderDetailDto |
| PATCH | `/orders/{id}/items` | Cart edit while NEW: `add` / `update` / `remove` (product ids); stock re-synced | CartEditRequest | OrderDetailDto |
| DELETE | `/orders/{id}` | Admin delete of a CANCELLED order; 409 otherwise | – | 204 |

### Suppliers (admin, plan §2)

| Method | Endpoint | Purpose | Request | Response |
|---|---|---|---|---|
| GET | `/suppliers` | Supplier list; `search` matches name | query params | page of SupplierDto |
| GET | `/suppliers/{id}` | Supplier detail incl. its addresses | – | SupplierDetailDto |
| POST | `/suppliers` | Create | SupplierWriteDto | 201 SupplierDetailDto |
| PUT | `/suppliers/{id}` | Update | SupplierWriteDto | SupplierDetailDto |
| DELETE | `/suppliers/{id}` | Delete; 409 while products still reference it | – | 204 |
| GET | `/suppliers/{id}/addresses` | List addresses | – | list of SupplierAddressDto |
| POST | `/suppliers/{id}/addresses` | Create address | SupplierAddressWriteDto | 201 SupplierAddressDto |
| PUT | `/supplieraddresses/{id}` | Update address | SupplierAddressWriteDto | SupplierAddressDto |
| DELETE | `/supplieraddresses/{id}` | Delete address | – | 204 |

### Business rules enforced in the service layer

- **Order status transitions**: `NEW → SHIPPED`, `NEW → CANCELLED`,
  `SHIPPED → DELIVERED`; everything else (and cancelling a shipped/delivered
  order) is 409. Cancelling a `NEW` order returns the reserved stock.
- **Stock**: never negative. Checkout and cart edits lock the affected
  `products` rows with `SELECT ... FOR UPDATE`; insufficient stock aborts the
  whole transaction with 409 (ProblemDetail carries `productId`, `available`,
  `requested`). Cart lines can only be changed while the order is `NEW`.
- **Order delete**: only `CANCELLED` orders; the service deletes the items
  first (the provided schema's FK has no `ON DELETE CASCADE`).
- **Optimistic locking**: `PUT /products/{id}/price` rejects a stale
  `version` with 409; concurrent transactions are also caught by JPA's
  `@Version` check.
- **Contact matching**: the schema has no FK between `customers` and
  `contacts`; a customer's contact record is the contact with the same email
  (generated data may contain duplicates, the lowest id is used
  deterministically).
- **Aggregation semantics**: `total_spent` in the customer detail and
  top-spenders responses (from the `customer_summary` view) excludes
  CANCELLED orders; `order_count` counts every order record. Order totals per
  order (`order_totals` view) sum all lines of that order regardless of status.
- **Product delete**: intentionally not exposed (plan §2); admin
  "remove from sale" is a stock/price update.
- **Customer delete/update**: intentionally not exposed (plan §2) — customers
  do not edit their identity record through the shop API.

## Requirements

- Java 21, Maven
- MariaDB ≥ 10.3 (the temporal feature needs system versioning) with the
  provided `webstore` schema loaded
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

- API: `http://localhost:8080/products/1` etc. (endpoints above)
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI spec: `http://localhost:8080/v3/api-docs`

## Database setup (DBA, once)

Run the schema changes/scripts as an admin account, never as the application
user, **before** starting the app (Hibernate runs `ddl-auto: validate` and
will fail on a mismatched schema):

```bash
mariadb -h 127.0.0.1 -u root -p < webstore-api/src/main/resources/db/grants.sql
mariadb -h 127.0.0.1 -u root -p < webstore-api/src/main/resources/db/product_subtypes.sql
mariadb -h 127.0.0.1 -u root -p webstore < webstore-api/src/main/resources/db/optimistic_locking.sql
mariadb -h 127.0.0.1 -u root -p webstore < webstore-api/src/main/resources/db/indexes.sql
mariadb -h 127.0.0.1 -u root -p webstore < webstore-api/src/main/resources/db/views.sql
mariadb -h 127.0.0.1 -u root -p webstore < webstore-api/src/main/resources/db/triggers.sql
mariadb -h 127.0.0.1 -u root -p webstore < webstore-api/src/main/resources/db/events.sql
mariadb -h 127.0.0.1 -u root -p -e "SET GLOBAL event_scheduler = ON"
```

- `product_subtypes.sql` adds the `products.dtype` discriminator and the two
  subclass tables for the `JOINED` inheritance case
  (see [PROJECT_PLAN.md §3](PROJECT_PLAN.md#3-domain-model--entity-mappings)).
- `optimistic_locking.sql` adds the `products.version` column used by the
  `@Version` optimistic locking (plan §5).
- `triggers.sql` seeds `productpricehistory` with the existing products'
  current prices (the temporal feature's first version).
- **Event scheduler**: `SET GLOBAL event_scheduler = ON` is not durable across
  server restarts — for a persistent setup set `event_scheduler=ON` in the
  MariaDB server configuration (e.g. `/etc/my.cnf.d/server.cnf`, `[mysqld]`)
  and restart. Without it the nightly `dailysales` event stays idle.

Passwords are never committed to this repository; the application reads
`WEBSTORE_DB_PASSWORD` / `WEBSTORE_DB_USER` (default: `webstore_app`) from
the environment. See also [PROJECT_PLAN.md §6](PROJECT_PLAN.md#6-database-features-views-triggers-indexing-temporal-security-backups).

## Security

- **Least privilege**: the API runs as `webstore_app` with exactly the
  grants in [grants.sql](webstore-api/src/main/resources/db/grants.sql) —
  SELECT everywhere it reads; INSERT/UPDATE (DELETE only on orders/its items
  and addresses) where it writes; read-only grants on the
  views/`dailysales`; **no write grants at all** on the trigger-written
  history/log tables (they fill via DEFINER rights), no root, no
  database-level INSERT/UPDATE/DELETE.
- **No secrets in VCS**: the DB password comes from `WEBSTORE_DB_PASSWORD`;
  the repo also contains no other credentials.
- **Data integrity at DB level**: FKs enforce referential rules the API can't
  bypass; `ddl-auto: validate` fails fast if the schema and the entities drift.
- **No API authentication** (course scope): admin/customer roles are handled
  by the documented rules and endpoint sets; see Limitations.

## Backup & schema changes

**Backup** (full logical backup incl. events/triggers/views data-independent):

```bash
mariadb-dump -h 127.0.0.1 -u root -p webstore \
    --triggers --events --single-transaction \
    > "backup/webstore-$(date +%F).sql"
```

- `--single-transaction` gives a consistent snapshot without locking reads.
- Restore = recreate the database, run the dump, then run the DBA scripts in
  the [setup order](#database-setup-dba-once) (views/triggers/events/indexes
  are DDL objects the data dump of a running schema may also recreate; check
  with `SHOW TRIGGERS`/`SHOW EVENTS` after restore).
- Suggested policy for the course scale: automatic nightly dump to
  `backup/` + weekly off-machine copy; nightly event keeps `dailysales`
  as a bonus management view.

**Schema-change procedure** (used for `product_subtypes.sql`,
`optimistic_locking.sql`):

1. Add an idempotent script (`IF NOT EXISTS`/guarded ALTERs) under
   `webstore-api/src/main/resources/db/` with a header on what/why/rollback.
2. Apply as DBA **before** deploying the app — the app boot validates the
   schema and refuses to start on mismatch (`ddl-auto: validate`).
3. Rollback = restore the schema from a backup (or reverse the ALTER);
   application data written in between is the operator's call — documented
   per script which changes are additive (safe) vs destructive.

## Limitations & assumptions

- **No authentication/authorization on the API** (course scope): any client is
  treated as the shop user; "admin" is an endpoint-set distinction, enforced
  by documented business rules rather than roles. HTTPS/rate limiting is out
  of scope.
- **No automated tests** — every feature was verified manually through the
  API/DB and the evidence is per documented step; no unit/integration test
  suite is committed.
- **Temporal history starts at deployment**: prices before
  `productpricehistory` existed cannot be answered from history; the
  documented fallback (`source=CURRENT_FALLBACK`) is used.
- **Contacts ↔ customers have no FK** in the provided schema; the API matches
  a customer's contact by identical email (deterministically the lowest id on
  duplicates).
- **Products cannot be deleted** (1.1 M `orderitems` references), admin
  "remove from sale" = price/stock update; a referenced customer address
  cannot be deleted (409).
- **Bulk price update bypasses** the persistence context and entity listeners
  (single native UPDATE; `version` bumped in-line, history captured at DB
  level) — documented in the endpoint table/`ProductRepository` javadoc.
- The nightly `dailysales` event needs `event_scheduler=ON` (see setup).
- Catalogue search matches product names only (`name contains`); the schema's
  description text is returned but not indexed for search.
