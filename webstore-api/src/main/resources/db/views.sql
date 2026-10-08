-- webstore-api: views serving API reads (PROJECT_PLAN.md §6).
--
-- Run as DBA/admin (DEFINER = admin; the app user gets SELECT-only grants, see
-- grants.sql):
--   mariadb -h 127.0.0.1 -u root -p webstore < webstore-api/src/main/resources/db/views.sql

USE webstore;

-- One row per order with customer name + item count + total amount.
-- Serves the order list / order summary part of the API (plan §4 #9).
-- Aggregates over orders + orderitems + products-related data; the
-- correlated scalar subqueries hit the orderitems PK prefix
-- (order_id, product_id), so each lookup is an index range scan, not a scan.
CREATE OR REPLACE VIEW order_totals AS
SELECT o.id                                   AS order_id,
       o.customer_id                          AS customer_id,
       CONCAT(c.first_name, ' ', c.last_name) AS customer_name,
       o.order_date                           AS order_date,
       o.delivery_date                        AS delivery_date,
       o.status                               AS status,
       (SELECT COALESCE(SUM(oi.quantity), 0)
              FROM orderitems oi WHERE oi.order_id = o.id)          AS item_count,
       (SELECT COALESCE(SUM(oi.quantity * oi.unit_price), 0)
              FROM orderitems oi WHERE oi.order_id = o.id)          AS total_amount
FROM orders o
JOIN customers c ON c.id = o.customer_id;

-- Per-customer aggregates (order count, non-cancelled total spend, latest
-- order date). Serves GET /customers/{id} (combined response, plan §4 #6)
-- and the top-spenders ranking (#8). With a WHERE customer_id = ? predicate
-- the joins are FK-index driven.
CREATE OR REPLACE VIEW customer_summary AS
SELECT c.id                                   AS customer_id,
       CONCAT(c.first_name, ' ', c.last_name) AS customer_name,
       c.email                                AS customer_email,
       COUNT(o.id)                            AS order_count,
       COALESCE(SUM(CASE WHEN o.status <> 'CANCELLED'
                         THEN oi.quantity * oi.unit_price END), 0) AS total_spent,
       MAX(o.order_date)                      AS latest_order_date
FROM customers c
LEFT JOIN orders o      ON o.customer_id = c.id
LEFT JOIN orderitems oi ON oi.order_id  = o.id
GROUP BY c.id, c.first_name, c.last_name, c.email;
