-- webstore-api: indexes for identified hot queries (PROJECT_PLAN.md §6).
--
-- Baselines and EXPLAIN before/after measurements: docs/index_plan.md.
-- Existing indexes from the provided schema are NOT duplicated here:
--   PKs on every table, FK lookups (fk_product_category, fk_product_supplier,
--   fk_order_customer, fk_order_shipping_address, fk_orderitem_order/
--   _product, fk_customeraddress_customer, fk_supplieraddress_supplier) —
--   these cover the join paths (see EXPLAIN control query in index_plan.md).
--
-- Run as DBA/admin:
--   mariadb -h 127.0.0.1 -u root -p webstore < webstore-api/src/main/resources/db/indexes.sql

USE webstore;

-- Q1: customer email lookup (login/account view; 100k rows) — doc/index_plan.md Q1
CREATE INDEX IF NOT EXISTS ix_customers_email ON customers (email);

-- Q2: order filtering by status + date range (order list/search; 200k rows)
-- single composite index instead of two: status equality first, then range
CREATE INDEX IF NOT EXISTS ix_orders_status_orderdate ON orders (status, order_date);

-- Q3: product name search (catalogue search box; prefix LIKE searches are
-- range scans on the index after the prefix)
CREATE INDEX IF NOT EXISTS ix_products_name ON products (name);

-- Q4: product price range filter (catalogue price slider)
CREATE INDEX IF NOT EXISTS ix_products_price ON products (price);

-- NOTE: orders.order DATE-range search alone (?dateFrom=&dateTo= with no
-- status) cannot use ix_orders_status_orderdate because status is the leading
-- column; if that access pattern matters in practice, a (order_date) index —
-- or reordering this index — should be added after measurement. Deliberately
-- not added now: the API search is mostly status-filtered (plan §4 #12).
