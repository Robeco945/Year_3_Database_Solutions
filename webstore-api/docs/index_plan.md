# Index plan & query-optimization notes

Supports [PROJECT_PLAN.md §6](../PROJECT_PLAN.md) ("Indexing & query
optimization") — the parts of the DB solution not visible through the API.
Measured on the populated course database
(contacts ≈ 500k, orderitems ≈ 1.1M, orders ≈ 200k, customers ≈ 100k,
products = 1 000 rows), MariaDB 13.0.2.

## Identified hot queries (from the planned API access patterns)

| # | API access pattern | Query shape |
|---|---|---|
| Q1 | account view by email | `customers WHERE email = ?` (const lookup) |
| Q2 | order list/search by status + date | `orders WHERE status = ? AND order_date >= ?` |
| Q3 | catalogue name search | `products WHERE name LIKE 'prefix%'` |
| Q4 | catalogue price filter | `products WHERE price BETWEEN ? AND ?` |
| Q5 (control) | order history by customer | `orders WHERE customer_id = ?` |

## Created indexes (`db/indexes.sql`)

| Index | Table | Rationale |
|---|---|---|
| `ix_customers_email` | customers(email) | const lookups on the account path; 100k rows |
| `ix_orders_status_orderdate` | orders(status, order_date) | composite: equality predicate first, then range — lets the optimizer `range`-scan straight to the status/date window instead of scanning 200k rows |
| `ix_products_name` | products(name) | catalogue search; also useful for ORDER BY on small ranges |
| `ix_products_price` | products(price) | price slider filter / price-sorted listings |
| `ix_orderstatuslog_order` | orderstatuslog(order_id, changed_at) | history-by-order reads from the §6 trigger log |

Deliberately **not** created now (documented as a decision, not an omission):

- `orders(order_date)` alone — the search API always filters on status first
  (plan §4 #12); a date-only range cannot use
  `ix_orders_status_orderdate` (leading `status`). If a date-only search
  pattern becomes hot, measure and add it then.
- Composite `(product_id, order_id)` on `orderitems` — the PK
  `(order_id, product_id)` plus the existing `fk_orderitem_product` index cover
  both href join directions (`EXPLAIN` below).

Already existing from the provided schema (not duplicated): every PK, and the
FK indexes `fk_order_customer`, `fk_orderitem_product`, `fk_orderitem_order`,
`fk_product_category`, `fk_product_supplier`, `fk_customeraddress_customer`,
`fk_supplieraddress_supplier`, `fk_order_shipping_address`.

## Measurements — EXPLAIN before / after

Run before `db/indexes.sql` was applied and again after; full outputs copied
verbatim. (Scanner: `EXPLAIN` in MariaDB 13.0.2.)

### Q1 — customers.email lookup

before:

| id | select_type | table | type | possible_keys | key | ref | rows | Extra |
|---|---|---|---|---|---|---|---|---|
| 1 | SIMPLE | customers | **ALL** | NULL | NULL | NULL | **99 688** | Using where |

after:

| id | select_type | table | type | possible_keys | key | ref | rows | Extra |
|---|---|---|---|---|---|---|---|---|
| 1 | SIMPLE | customers | ref | ix_customers_email | ix_customers_email | const | **1** | Using index condition |

**~99 700× fewer rows examined**; full-table scan → single index entry.

### Q2 — orders by status (+ date range)

before:

| id | select_type | table | type | possible_keys | key | rows | Extra |
|---|---|---|---|---|---|---|---|
| 1 | SIMPLE | orders | **ALL** | NULL | NULL | **199 806** | Using where |

after:

| id | select_type | table | type | possible_keys | key | rows | Extra |
|---|---|---|---|---|---|---|---|
| 1 | SIMPLE | orders | **range** | ix_orders_status_orderdate | ix_orders_status_orderdate | **6 010** | Using where; **Using index** |

**~33× fewer rows, covering index** (the COUNT(*) only needs indexed columns —
no row lookups at all).

### Q3 — product name prefix search

before: `type=ALL`, **1 000** rows. after: `type=range`, **2** rows on
`ix_products_name`. Small table, but the prefix-LIKE search uses the index
`range` scan and stays correct as the catalogue grows.

### Q4 — product price range

before: `type=ALL`, **1 000** rows. after: `type=range`, **58** rows on
`ix_products_price`.

### Q5 — control (orders.customer_id)

Identical before and after — `type=ref`, 1 row via the **existing** schema
index `fk_order_customer`, `Using index`. Shows the provided schema's FK
indexes already serve the join paths; only the filter/sort paths above needed
new indexes. This is the "do not blindly index everything" part of the plan:
each index is justified by a measured access pattern, because extra indexes
cost write performance (relevant for checkout §5, where `stock_quantity`
updates and order inserts happen in one transaction).

## Notes

- `EXPLAIN` was also re-run after the §6 views were created: the views'
  correlated scalar subqueries hit the `orderitems` PK prefix
  `(order_id, product_id)` (index range scan per order), which is why no
  separate index is planned for the view paths.
- The nightly `dailysales` event aggregates a date-slice of `orders` joined
  `orderitems` (few thousand orders per day) — a table scan per event run is
  acceptable at this scale; if it ever matters, the composite status/date
  index or a generated `order_date_date` column could help.
