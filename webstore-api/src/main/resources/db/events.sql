-- webstore-api: nightly scheduled event (PROJECT_PLAN.md §6).
--
-- Daily sales summary: one row per sale date with order count and (organic)
-- total amount. CANCELLED orders are excluded from the sale figures.
--
-- ⚠ Event scheduler requirement: MariaDB's event_scheduler must be ON for the
-- event to run. One-time enable (needs admin/SUPER):
--   SET GLOBAL event_scheduler = ON;
-- To make it survive a server restart, set `event_scheduler=ON` in the
-- MariaDB server configuration (e.g. /etc/my.cnf.d/server.cnf [mysqld])
-- — the SET GLOBAL alone does not persist. Documented in README.
--
-- Run as DBA/admin (DEFINER = admin; the app user only reads dailysales, see
-- grants.sql):
--   mariadb -h 127.0.0.1 -u root -p webstore < webstore-api/src/main/resources/db/events.sql

USE webstore;
DELIMITER //

CREATE TABLE IF NOT EXISTS dailysales (
	sale_date    DATE NOT NULL,
	order_count  INT NOT NULL,
	total_amount DECIMAL(14,2) NOT NULL,
	PRIMARY KEY (sale_date)
) ENGINE = InnoDB;

CREATE EVENT IF NOT EXISTS ev_daily_sales_yesterday
ON SCHEDULE EVERY 1 DAY
STARTS (TIMESTAMP(CURRENT_DATE) + INTERVAL 1 DAY + INTERVAL 3 HOUR)
ON COMPLETION PRESERVE
COMMENT 'Refresh the dailysales row of yesterday (excluding CANCELLED orders)'
DO
BEGIN
	-- idempotent refresh of exactly yesterday's slice
	DELETE FROM dailysales WHERE sale_date = CURRENT_DATE - INTERVAL 1 DAY;
	INSERT INTO dailysales (sale_date, order_count, total_amount)
	SELECT DATE(o.order_date)                              AS sale_date,
	       COUNT(DISTINCT o.id)                            AS order_count,
	       COALESCE(SUM(oi.quantity * oi.unit_price), 0)   AS total_amount
	FROM orders o
	JOIN orderitems oi ON oi.order_id = o.id
	WHERE o.status <> 'CANCELLED'
	  AND o.order_date >= CURRENT_DATE - INTERVAL 1 DAY
	  AND o.order_date <  CURRENT_DATE
	GROUP BY DATE(o.order_date);
END //

DELIMITER ;

-- Optional one-off backfill (e.g. for verifying the event logic locally):
--   INSERT INTO dailysales (sale_date, order_count, total_amount)
--   SELECT DATE(o.order_date), COUNT(DISTINCT o.id),
--          COALESCE(SUM(oi.quantity * oi.unit_price), 0)
--   FROM orders o JOIN orderitems oi ON oi.order_id = o.id
--   WHERE o.status <> 'CANCELLED'
--   GROUP BY DATE(o.order_date);
