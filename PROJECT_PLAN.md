# 📋 Project Plan: Webshop Order Management REST API — Spring Boot + JPA + MariaDB

Based on the sample repo: [`tkr_springbootexample`](https://github.com/vesavvo/tkr_springbootexample)
Target database: **`webstore`** (schema in `Project Data/`: clean / populated dumps)

> **How this plan maps to the course instructions** — see
> [§10 Compliance with course requirements](#10-compliance-with-course-requirements).

---

## 1. Project definition

Build an **order management system / webshop back-end service** for the `webstore`
MariaDB database, exposed as a **REST API** implemented with Spring Boot in the same
package structure as `tkr_springbootexample`.

**Chosen perspective:** the **webshop view for end users (customers)** — browsing
products, placing orders and managing one's own orders, addresses and contact
information. Where this end-user view would not give complete CRUD on a resource
(products, categories, suppliers), a **complementary admin view** fills the gaps so
that every key resource has full CRUD somewhere in the API. This choice is stated
and justified in the README, as required.

Every course topic is represented by at least one concrete endpoint or feature,
modeled directly on the sample repo's patterns.

| Sample repo package | Purpose in our project |
|---|---|
| `entity` | JPA entities mapped to webstore tables |
| `controller` | REST endpoints (one controller per resource) |
| `service` | Business logic, transactions, bulk operations |
| `repository` | Spring Data JPA repositories (derived queries + `@Query`) |
| `dto` | Data Transfer Objects for clean API responses |
| `converter` | `AttributeConverter` handling |
| `entitylistener` | JPA event callbacks (`@PostPersist`, `@PreUpdate`, ...) |

**Final deliverable:** working API backend **with its documentation** —
OpenAPI / Swagger UI via `springdoc-openapi`, plus a README that documents every
endpoint (purpose, request and response format) and all DB-side features not
visible through the API.

---

## 2. CRUD on key resources

The key resources of the order management system, and the CRUD coverage of each.
Rules enforced in the service layer (e.g. when an order may be modified) are
documented per endpoint in the README.

| Resource | Table | CRUD coverage |
|---|---|---|
| **Customer** | `customers` | **R** (customer view). `GET /customers/{id}` returns the customer **combined with data from several tables** (addresses, order history: order count, total spend — see §4). Rationale: customers should not be able to edit their identity record through the shop API |
| **Contact** | `contacts` | **C-R-U** — a customer's contact email can be created, read and updated (`/customers/{id}/contact`) |
| **Product** | `products` | **C-R-U** — R for everyone (search/filter across * name, category *), C/U via admin view. No D (a product referenced by 1.1M `orderitems` rows must not disappear); admin "remove from sale" is modelled as U (e.g. price/stock change) |
| **Product category** | `productcategories` | **C-R-U-D** (admin view) — small table (10 rows), safely deletable category |
| **Supplier** | `suppliers` | **C-R-U-D** (admin view), incl. its `supplieraddresses` (1:M) |
| **Order** | `orders` | **C** (checkout) / **R** (single, list, per-customer, filtered by status/date) / **U** (change status: `NEW → SHIPPED → DELIVERED`, cancelling only while `NEW`; change delivery date) / **D** (admin delete of a cancelled order) |
| **Order item** | `orderitems` | C + U + D only **through the order aggregate**: add/change quantity/remove items while the order is still `NEW` (cart editing) |
| **Customer address** | `customeraddresses` | **C-R-U-D** — customers manage their own delivery addresses; an address referenced as `shipping_address_id` cannot be deleted (FK constraint) |

The **order aggregate** (`Order` + its items) is the most important C/U path and is
implemented transactionally (§5).

---

## 3. Domain model & entity mappings

The `webstore` schema (9 tables: `contacts`, `customers`, `customeraddresses`,
`suppliers`, `supplieraddresses`, `productcategories`, `products`, `orders`,
`orderitems` — ~100k customers, ~1.1M order items, generated data) maps naturally
to all planned JPA concepts:

| JPA concept | Mapped to |
|---|---|
| **@Entity** | `Contact`, `Customer`, `CustomerAddress`, `Supplier`, `SupplierAddress`, `ProductCategory`, `Product`, `Order`, `OrderItem` |
| **1:M** | `Customer→CustomerAddresses`, `Supplier→SupplierAddresses`, `ProductCategory→Products`, `Supplier→Products`, `Order→OrderItems`, `Customer→Contacts` |
| **N:M** | `Orders ↔ Products` (through the `orderitems` link table; `orderitems` uses a composite PK `(order_id, product_id)`) |
| **1:1** | `Order ↔ CustomerAddress` (via `orders.shipping_address_id` FK) |
| **Inheritance** | `Contact` extended by a customer-contact subtype, **or** `Product` → `PhysicalProduct` / `DigitalProduct` (e.g. `JOINED` strategy) — final choice is justified in the README |
| **Eager vs Lazy** | `@ManyToOne` (product → category/supplier) = **eager**; `@OneToMany` (order → items, customer → addresses/orders) = **lazy**; justification in README |
| **Converter** | `OrderStatus` (`NEW`, `SHIPPED`, `DELIVERED`, `CANCELLED`) as enum ↔ `VARCHAR` via `AttributeConverter`, **or** a boolean ↔ `Y/N` converter (mirroring the sample repo's `KyllaEiBooleanConverter`); choice justified in README |
| **Entity listener** | `@PreUpdate` / `@PostPersist` on `Product` (log price/stock changes, mirroring `HaltijaListener`) and/or on `Order` |

---

## 4. Endpoint plan (feature → endpoint)

A "broad" API: endpoints comprehensively cover the key resources and **combine data
from different tables**, as required. Every endpoint's purpose, request format and
response format are documented per endpoint in the README and in the
OpenAPI/Swagger spec.

### Query & search — combining tables

| # | Endpoint | Purpose / technique |
|---|---|---|
| 1 | `GET /products` | Catalogue listing + filters: `?category=&supplier=&maxPrice=&minPrice=&search=&page=&size=` (multi-table joins via JPA criteria / `@Query`). **Pagination + sorting (Spring Data `Pageable`)** |
| 2 | `GET /products/{id}` | Product detail (joins: category, supplier name) |
| 3 | `GET /products/price/{min}` | Simple single-table query (derived query) |
| 4 | `GET /products/category/{id}` | Products in a category with the category's own info included |
| 5 | `GET /products/stats/by-category` | Per-category product count / avg / min / max price: **GROUP BY + HAVING + aggregate functions** |
| 6 | `GET /customers/{id}` | Customer detail **combining `customers` + `contacts` + `customeraddresses` + `orders`** (contact email, addresses, order count, total spent) |
| 7 | `GET /customers/without-orders` | **LEFT JOIN / subquery** (customers missing from `orders`) |
| 8 | `GET /customers/top-spenders` | JOIN `customers`–`orders`–`orderitems` + **GROUP BY + ORDER BY SUM** (also feeds the nightly event, §6) |
| 9 | `GET /orders` | Order list via the **`order_totals` view** (§6): id, date, status, customer name, item count, total; filter `?status=&customerId=&page=` |
| 10 | `GET /orders/{id}` | Order detail with items, product names, subtotal per item and order total: **multi-table join over `orders`+`orderitems`+`products`** |
| 11 | `GET /orders/customer/{id}` | A customer's order history — **INNER JOIN** (also supports `?status=` filtering) |
| 12 | `GET /orders/search` | Multiple criteria: `?status=&dateFrom=&dateTo=` (**dynamic query/Criteria API**) |

### Write operations

| # | Endpoint | Purpose / technique |
|---|---|---|
| 13 | `POST /orders` | **Checkout**: creates order + items and **decrements `stock_quantity` atomically** in one `@Transactional` service method; `SELECT … FOR UPDATE` pessimistic row lock on stock; 409 on insufficient stock |
| 14 | `PUT /orders/{id}/status` | Order status transition (rules in README: cancel only while `NEW` etc.) |
| 15 | `PATCH /orders/{id}/items` | Cart editing while `NEW`: add item / change quantity / remove item; re-syncs stock transactionally |
| 16 | `POST /products` / `PUT /products/{id}` | Admin create/update product (C/U for products) |
| 17 | `PUT /products/{id}/price` | Price change — goes through the price-history trigger chain (§6) and demo `@Version` optimistic locking (§5) |
| 18 | `DELETE /products/category/{id}` | Full CRUD cycle for the small category table |
| 19 | `POST /customers/{id}/addresses`, `PUT /customeraddresses/{id}`, `DELETE /customeraddresses/{id}` | Full CRUD for delivery addresses |
| 20 | `POST /customers/{id}/contact`, `PUT /contact/{id}` | CRUD for the customer's contact data |
| 21 | `DELETE /orders/{id}` | Admin delete of a cancelled order (cascade via `ON DELETE CASCADE`) |

---

## 5. Transaction, locking, versioning

- **`POST /orders`** (#13): the whole checkout runs in one transaction
  (`@Transactional`): insert `orders` row → insert `orderitems` rows →
  `UPDATE products SET stock_quantity = stock_quantity - ?`. **Pessimistic row lock**
  (`SELECT … FOR UPDATE` via repository method) on the involved `products` rows so
  two concurrent orders cannot oversell the same stock. Rollback on any failure.
- **`PATCH /orders/{id}/items`** (#15): same transactional pattern extended to cart
  editing (return reserved stock when an item is removed).
- **`@Version` optimistic locking** on `Product`: price/stock updates carrying a
  stale version fail with `OptimisticLockException` → 409. README discusses when
  automatic transactions / row locks suffice vs a manual lock.
- A small **demo of nested/separate transactions** (e.g. order + separate audit
  write) can be used to illustrate propagation, discussed in README.

---

## 6. Database features (views, triggers, indexing, temporal, security, backups)

These features are not all visible through the API, so **they are all explicitly
documented in the README** as the instructions require.

### Indexing & query optimization

- Identified frequent/hot queries: product price search (`products.price`),
  product name search (`products.name`), customer email (`customers.email`),
  order status/date filtering and `orders.customer_id` joins.
- Measure with **`EXPLAIN` before/after** adding indexes; document results and
  rationale in `docs/index_plan.md` (referenced from the README).
- FK columns (`customer_id`, `category_id`, `product_id`, …) already indexed by the
  schema; the plan adds composite/covering indexes where `EXPLAIN` shows table scans.

### Views

- `order_totals` — one row per order: order id, date, status, customer name,
  **item count, total amount** (aggregates `orders`+`orderitems`+`products`).
  Serves `GET /orders` (#9; faster, cleaner than entity-graph aggregation).
- `customer_summary` — per-customer aggregates (order count, total spent,
  latest order date), serving `GET /customers/{id}` (#6) and `top-spenders` (#8).

### Triggers & events

- **Trigger**: on `products.price` UPDATE/INSERT → write old/new price into
  `productpricehistory` (also the temporal feature, below).
- **Trigger**: on `orders.status` UPDATE → write into `orderstatuslog`
  (order id, old → new status, timestamp) for order lifecycle statistics.
- **Scheduled event**: nightly MariaDB `CREATE EVENT` producing/complementing the
  daily sales summary (per day: sum of order totals); optional stored procedure
  (e.g. `cancel_order` = status change + stock return in one call).

### Temporal features

- Price history: the price trigger keeps `productpricehistory` up to date
  automatically; endpoint `GET /products/{id}/price-history` shows the history and
  `GET /products/{id}/price?at=` answers "price of product X at time T"
  (system-versioned / temporal query). Documentation includes example queries
  (e.g. `FOR SYSTEM_TIME AS OF` syntax if system-versioned tables are used, or
  the history-table approach if not).
- Orders already carry `order_date` / `delivery_date` timestamps; they are used in
  date-range searches.

### User accounts, security, backups

- **`CREATE USER 'webstore_app'@'localhost'`** with least-privilege
  `GRANT SELECT, INSERT, UPDATE` (+ `DELETE` on `orders`/`orderitems` only if
  actually needed) — the API connects with this user, never as root. Admin-level
  operations (creating triggers/events) are executed by a separate DBA account,
  documented in `docs/security.md`.
- Passwords are **not stored in the repo**; configuration uses environment
  variables.
- Backup plan and schema-change plan (how a schema change is rolled forward and
  back) documented in the README + `docs/security.md`.

### SQL scripts & reproducibility

- Views, triggers, events, indexes and grants are implemented via SQL scripts under
  `src/main/resources/db/` and applied as part of the setup (`data.sql` / init
  scripts), so a fresh checkout reproduces everything.

---

### Bulk operations & N+1

- **`@Modifying` JPQL bulk update**: e.g. category-wide pricing update
  (`UPDATE Product p SET p.price = p.price * :factor WHERE p.category.id = :categoryId`).
- **JOIN FETCH** repository method for order details to demonstrate **N+1
  avoidance** (measured with `EXPLAIN`/log compare, discussed in README).
- **Criteria API / Specification**-based dynamic search for the order search
  endpoint.

---

## 7. Project structure (mirroring the sample repo)

```
webstore-api/
├── src/main/java/fi/metropolia/<you>/webstoreapi/
│   ├── WebstoreApiApplication.java
│   ├── controller/     CustomerController, ProductController, ProductCategoryController,
│   │                   SupplierController, OrderController, CustomerAddressController
│   ├── service/        CustomerService, OrderService, ProductCategoryService, ...
│   ├── repository/     CustomerRepository, ProductRepository, ...
│   ├── entity/         Contact, Customer, CustomerAddress, Supplier, SupplierAddress,
│   │                   ProductCategory, Product, Order, OrderItem
│   ├── dto/            CustomerDto, OrderDto, OrderItemDto, ...
│   ├── converter/      OrderStatusConverter
│   └── entitylistener/ ProductListener, OrderListener
├── src/main/resources/
│   ├── application.yml
│   └── db/             indexes.sql, views.sql, triggers.sql, events.sql, grants.sql
├── docs/
│   ├── index_plan.md   (EXPLAIN before/after measurements)
│   ├── security.md     (users & grants, backup plan, schema-change plan)
│   └── temporal.md     (price history approach)
├── README.md           (endpoint docs: purpose/request/response + DB features + setup)
└── pom.xml             (spring-boot-starter-web, spring-boot-starter-data-jpa,
                        mariadb-java-client, springdoc-openapi)
```

---

## 8. Work order

1. **Scaffold** — ✅ *done: `webstore-api/` creates at the same stack as sample repo
   (Spring Boot 3.5.11, Java 21 + springdoc), `application.yml` configured for the
   webstore DB with the least-privilege `webstore_app` user (pw via
   `WEBSTORE_DB_PASSWORD` env var, never committed), `db/grants.sql` created, dump
   loaded locally; entity `Product` + endpoint `GET /products/{id}` verified
   end-to-end (200 / 404) with Swagger UI up.*
2. **Domain layer** — all 9 entities, repositories, DTOs; verify associations
   (1:M, N:M via `orderitems`, 1:1 shipping address, one inheritance case).
3. **CRUD + query endpoints** — implement the §2 CRUD coverage and §4 endpoint
   tables, starting with the order aggregate (checkout, cart editing, status
   changes).
4. **Database features** — views, triggers (price + status log), scheduled event,
   temporal price history, app user & grants; SQL scripts under
   `src/main/resources/db/`; `EXPLAIN` before/after measurements for the index plan.
5. **Documentation** — OpenAPI spec (springdoc), README (all endpoints: purpose,
   request, response; all DB features; limitations), `docs/` notes. Keep it current
   as features land.

---

## 9. Definition of done

- [ ] All 9 tables mapped to JPA entities; association types covered (1:M, N:M, 1:1) + inheritance
- [ ] Eager vs lazy loading choices justified in README
- [ ] **CRUD exists for every key resource** (see §2 table; restrictions and
      rationale documented in README)
- [ ] Queries of varying difficulty implemented (single table, inner join,
      left join/subquery, GROUP BY/HAVING, dynamic multi-criteria) — and endpoints
      **combine data from different tables**
- [ ] **Broad API**: comprehensive endpoints incl. pagination + multi-criteria search
- [ ] Identified hot queries backed by an indexing plan + before/after `EXPLAIN` measurements
- [ ] `POST /orders` demonstrates transactional stock handling with row locking;
      cart editing (`PATCH /orders/{id}/items`) also transactional
- [ ] Optimistic locking (`@Version`) implemented and evaluated
- [ ] ≥1 view (serving API responses), ≥1 trigger (price + status logs),
      ≥1 scheduled event (+ optional stored procedure)
- [ ] Temporal solution: price history queryable, "price at time T" endpoint
- [ ] App-level DB user with least-privilege grants; backup plan and schema-change
      plan documented
- [ ] ≥1 `AttributeConverter`, ≥1 entity listener, ≥1 bulk `@Modifying` JPQL query,
      N+1 avoidance demonstrated (JOIN FETCH)
- [ ] OpenAPI/Swagger served, and **README documents every endpoint (purpose,
      request, response) plus views, triggers, indexes, temporal features,
      security, backups** — everything implemented and working is visible from the
      documentation

---

## 10. Compliance with course requirements

| Course instruction | How this plan satisfies it |
|---|---|
| *Order management system / webshop back-end on a REST API using the given schema* | Spring Boot + JPA + MariaDB over the provided `webstore` schema (§1) |
| *Essential CRUD interfaces for key resources* | §2 CRUD table per resource, with justified restrictions; admin view completes CRUD where customer view doesn't |
| *Broad API serves the webshop view for end users; comprehensively uses and combines data from tables* | Chosen perspective stated in §1; §4 endpoints 1, 2, 5, 6, 8, 9, 10 combine `customers`, `contacts`, `customeraddresses`, `orders`, `orderitems`, `products`, `productcategories`, `suppliers`; pagination + multi-criteria search |
| *Versatile use of course techniques* | Views, triggers, events, indexing with EXPLAIN measurements, transactions + pessimistic/optimistic locking, temporal price history, least-privilege user/grants, converters, entity listeners, bulk JPQL, N+1 avoidance, inheritance, pagination (§3–§6) |
| *README documents all API endpoints (purpose, request, response)* | §4 note + work order step 5 + DoD; README is the primary deliverable |
| *README highlights implemented features and characteristics* | §6 features are explicitly README sections; DoD requires DB-feature documentation |
| *Describe parts not visible from the API (indexes, views, security, ...)* | §6 "not visible through the API, documented in README" items; DoD item on views/indexes/security/backups |
