-- webstore-api: schema change for the JPA optimistic locking case (PROJECT_PLAN.md §5)
-- @Version column on products: a price/stock update carrying a stale version
-- fails with an OptimisticLockException -> 409 (see Product entity, README).
--
-- Run ONCE as a DBA/admin account, before the application starts
-- (Hibernate runs with ddl-auto: validate):
--   mariadb -h 127.0.0.1 -u root -p < webstore-api/src/main/resources/db/optimistic_locking.sql

USE webstore;

ALTER TABLE products
	ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
