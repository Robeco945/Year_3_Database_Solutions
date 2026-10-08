# 📋 Project Plan: Webstore REST API — Spring Boot + JPA + MariaDB

Based on the sample repo: [`tkr_springbootexample`](https://github.com/vesavvo/tkr_springbootexample)
Target database: **`webstore`** (schema in `Project Data/`: clean / populated dumps)

---

## 1. Project definition

Build an **API backend for the `webstore` MariaDB database**, implemented as a
Spring Boot application in the same package structure as `tkr_springbootexample`.
Every topic is represented by at least one concrete endpoint or feature,
modeled directly on the sample repo's patterns.

| Sample repo package | Purpose in our project |
|---|---|
| `entity` | JPA entities mapped to webstore tables |
| `controller` | REST endpoints (one per feature/topic) |
| `service` | Business logic, transactions, bulk operations |
| `repository` | Spring Data JPA repositories (derived queries + `@Query`) |
| `dto` | Data Transfer Objects for clean API responses |
| `converter` | `AttributeConverter` handling |
| `entitylistener` | JPA event callbacks (`@PostPersist`, `@PreUpdate`, ...) |

**Final deliverable:** working API backend **with its documentation** —
OpenAPI / Swagger UI via `springdoc-openapi`.

---

## 2. Domain model & entity mappings

The webstore schema (9 tables, ~100k customers, ~500k contacts, generated data)
maps naturally to all planned JPA concepts:

| JPA concept | Mapped to |
|---|---|
| **@Entity** | `Customer`, `Supplier`, `CustomerAddress`, `SupplierAddress`, `ProductCategory`, `Product`, `Order`, `OrderItem`, `Contact` |
| **1:M** | `Customer→CustomerAddresses`, `Supplier→SupplierAddresses`, `ProductCategory→Products`, `Supplier→Products`, `Order→OrderItems` |
| **N:M** | `Orders ↔ Products` (through the `OrderItems` link table) |
| **1:1** | `Order ↔ shipping_address` (via `shipping_address_id` FK) |
| **Inheritance** | `Contact` extended by a customer subtype, or `Product` → `PhysicalProduct` / `DigitalProduct` (e.g. `JOINED` inheritance strategy) |
| **Eager vs Lazy** | `@ManyToOne` to category/supplier → **eager**; `@OneToMany` to orders/order items → **lazy** (justify the choice in README) |
| **Converter** | `OrderStatus` as `enum` ↔ `VARCHAR`, or a boolean ↔ `Y/N` converter (like the sample repo's `KyllaEiBooleanConverter`) |
| **Entity listener** | `@PreUpdate` / `@PostPersist` on `Product` or `Order` to log changes (mirrors `HaltijaListener`) |

---

## 3. Repository & endpoint plan (feature → endpoint)

| Feature area | Endpoint / implementation |
|---|---|
| **Queries of varying difficulty** | `GET /products/price/{min}` (one table), `GET /orders/customer/{id}` (inner join), `GET /customers/without-orders` (left join / subquery), `GET /products/stats/by-category` (GROUP BY/HAVING) |
| **Indexing & query optimization** | Frequent searches: product price, customer email, order status. Measure with `EXPLAIN`, create indexes, document rationale in `docs/index_plan.md` |
| **Transactions** | `POST /orders` — create order + decrement `stock_quantity` atomically under `@Transactional`, with `SELECT ... FOR UPDATE` |
| **Locks, versioning** | `@Version` optimistic locking on `Product`; discussion in README of when automatic transactions / row locks suffice vs a manual lock |
| **Views** | `CREATE VIEW order_summary AS ...` in SQL script, queried via `@Query(nativeQuery = true)` |
| **Triggers, procedures, events** | Trigger: log table for `products.price` changes; Event: nightly aggregate, e.g. daily sales summary. Optional stored procedure |
| **User accounts, security, backups** | `CREATE USER app_user` + `GRANT SELECT/INSERT/UPDATE` only; written up in `docs/security.md` |
| **Temporal features** | Price history: reuse the trigger log as a price-history table + query "price at time T" |
| **Bulk operations (JPQL, Criteria API), N+1** | `@Modifying @Query("UPDATE Product p SET p.price = p.price * :factor WHERE ...")`, plus a `JOIN FETCH` repository method demonstrating N+1 avoidance |
| **Converters, events, optimistic locking** | `OrderStatusConverter`, `ProductListener` entity listener, `@Version` |

---

## 4. Project structure (mirroring the sample repo)

```
webstore-api/
├── src/main/java/fi/metropolia/<you>/webstoreapi/
│   ├── WebstoreApiApplication.java
│   ├── controller/     CustomerController, ProductController, OrderController, ...
│   ├── service/        CustomerService, OrderService, ...
│   ├── repository/     CustomerRepository, ProductRepository, ...
│   ├── entity/         Customer, Product, Order, ...
│   ├── dto/            CustomerDto, OrderDto, ...
│   ├── converter/      OrderStatusConverter
│   └── entitylistener/ ProductListener
├── src/main/resources/
│   ├── application.yml
│   └── db/             indexes.sql, views.sql, triggers.sql, events.sql
├── docs/
│   ├── index_plan.md
│   ├── security.md     (user accounts, grants, backup plan, schema-change plan)
│   └── temporal.md     (price history approach)
├── README.md           (loading-choice justification, setup, endpoint list)
└── pom.xml             (spring-boot-starter-web, spring-boot-starter-data-jpa,
                        mariadb-java-client, springdoc-openapi)
```

---

## 5. Work order

1. **Scaffold** — create project at start.spring.io (same stack as sample repo),
   configure `application.yml` for the webstore DB, get one entity + one
   endpoint working end-to-end.
2. **Domain layer** — all 9 entities, repositories, DTOs; verify associations
   (1:M, N:M via `OrderItems`, 1:1 shipping address, one inheritance case).
3. **Feature endpoints** — implement the feature table above, in the order listed.
4. **Database features** — indexes, views, triggers, scheduled event, temporal
   price history, app user & grants, all as SQL scripts under `src/main/resources/db/`.
5. **Documentation** — OpenAPI spec (springdoc), `README.md`, `docs/` notes;
   keep it current as features land.

---

## 6. Definition of done

- [ ] All 9 tables mapped to JPA entities, all association types covered (1:M, N:M, 1:1) + inheritance
- [ ] Eager vs lazy loading choices justified in README
- [ ] Queries of varying difficulty implemented (single table, joins, GROUP BY/HAVING, subquery)
- [ ] Identified hot queries backed by an indexing plan + before/after performance measurements
- [ ] `POST /orders` demonstrates transactional stock handling with row locking
- [ ] Optimistic locking (`@Version`) evaluated/implemented
- [ ] ≥1 view, ≥1 trigger (price log), ≥1 scheduled event (+ optional stored procedure)
- [ ] Temporal solution: price history queryable, "price at time T" endpoint or query
- [ ] App-level DB user with least-privilege grants; backup plan and schema-change plan documented
- [ ] ≥1 `AttributeConverter`, ≥1 entity listener, ≥1 bulk `@Modifying` query
- [ ] OpenAPI/Swagger documentation served and API spec complete
