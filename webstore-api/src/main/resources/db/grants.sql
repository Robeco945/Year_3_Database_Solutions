-- webstore-api: least-privilege grants for the REST API application user.
--
-- Run this ONCE as a DBA/admin account (NOT as the app user), e.g.:
--   mariadb -h <host> -u root -p < grants.sql
-- Passwords are never stored in this repository:
--   - create the user manually (password chosen locally / via env var), or run
--     the CREATE USER below with your own password substituted, then comment
--     it out / delete it after first use.
--
-- Plan reference: PROJECT_PLAN.md §6 "User accounts, security, backups".

-- database must exist before granting
CREATE DATABASE IF NOT EXISTS webstore CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- === one-time setup, do not store the real password here ===
-- CREATE USER 'webstore_app'@'localhost' IDENTIFIED BY '<password_set_locally>';
-- CREATE USER 'webstore_app'@'127.0.0.1' IDENTIFIED BY '<password_set_locally>';

-- read access everywhere: catalogue, addresses, contacts, orders
GRANT SELECT ON webstore.orders          TO 'webstore_app'@'localhost';
GRANT SELECT ON webstore.orderitems      TO 'webstore_app'@'localhost';
GRANT SELECT ON webstore.products        TO 'webstore_app'@'localhost';
GRANT SELECT ON webstore.customers       TO 'webstore_app'@'localhost';
GRANT SELECT ON webstore.contacts        TO 'webstore_app'@'localhost';

-- write access needed by the API today:
--   products      : admin create/update (incl. stock decrement on checkout)
--   productcategories : full CRUD cycle of the small category table (plan §4 #18)
--   suppliers / supplieraddresses : admin CRUD incl. 1:M addresses (plan §2)
--   orders        : checkout (insert) + status update + delete of cancelled orders
--   orderitems    : checkout items, cart editing, cascade delete with order
--   customeraddresses : customers manage their own addresses (incl. delete)
--   customers     : future admin create/update (plan §2)
--   contacts      : customer contact CRUD (plan §2)
GRANT SELECT, INSERT, UPDATE ON webstore.products             TO 'webstore_app'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON webstore.productcategories TO 'webstore_app'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON webstore.suppliers    TO 'webstore_app'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON webstore.supplieraddresses TO 'webstore_app'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON webstore.orders       TO 'webstore_app'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON webstore.orderitems   TO 'webstore_app'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON webstore.customeraddresses TO 'webstore_app'@'localhost';
GRANT SELECT, INSERT, UPDATE ON webstore.customers            TO 'webstore_app'@'localhost';
GRANT SELECT, INSERT, UPDATE ON webstore.contacts             TO 'webstore_app'@'localhost';

-- product subtype tables (JOINED inheritance; db/product_subtypes.sql):
-- product "remove-from-sale"/admin updates may touch subclass attributes
GRANT SELECT, INSERT, UPDATE ON webstore.physicalproducts TO 'webstore_app'@'localhost';
GRANT SELECT, INSERT, UPDATE ON webstore.digitalproducts  TO 'webstore_app'@'localhost';

-- Bulk @Modifying updates: implemented as a single-table NATIVE update on
-- products (see ProductRepository javadoc). A JPQL bulk update on the JOINED
-- Product hierarchy would go through Hibernate's multi-table strategy and need
-- temporary-table privileges plus a same-named base table — verified live and
-- rejected as a correctness hazard (rows accumulate in the stub table).

-- §6 feature objects (created by db/views.sql, db/triggers.sql, db/events.sql):
-- views serving API reads; history/log/summary tables are read by the API and
-- WRITTEN ONLY by the triggers/event (DEFINER = admin) — the app needs no
-- write grants on them. Least privilege stays intact.
GRANT SELECT ON webstore.order_totals        TO 'webstore_app'@'localhost';
GRANT SELECT ON webstore.customer_summary    TO 'webstore_app'@'localhost';
GRANT SELECT ON webstore.productpricehistory TO 'webstore_app'@'localhost';
GRANT SELECT ON webstore.orderstatuslog      TO 'webstore_app'@'localhost';
GRANT SELECT ON webstore.dailysales          TO 'webstore_app'@'localhost';

FLUSH PRIVILEGES;
