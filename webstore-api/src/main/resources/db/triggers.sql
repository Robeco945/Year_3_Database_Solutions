-- webstore-api: triggers + the temporal feature (PROJECT_PLAN.md §6).
--
-- Temporal approach: MariaDB SYSTEM VERSIONING (supported since MariaDB 10.3,
-- required for the productpricehistory table and all FOR SYSTEM_TIME queries).
-- Fully script-reproducible on a second machine — same MariaDB, same scripts.
--
-- Run as DBA/admin (triggers run with DEFINER rights, so the app user can
-- change a price without needing write grants on the history table):
--   mariadb -h 127.0.0.1 -u root -p webstore < webstore-api/src/main/resources/db/triggers.sql

USE webstore;

-- ---------------------------------------------------------------- price history
-- One versioned row per product (PK product_id); every price UPDATE of the row
-- is kept by system versioning. Derived queries the API can use:
--   price now:            SELECT price FROM productpricehistory WHERE product_id = ?
--   price at time T:      SELECT price FROM productpricehistory
--                         FOR SYSTEM_TIME AS OF 'T' WHERE product_id = ?
--   change log:           SELECT price, ROW_START, ROW_END FROM productpricehistory
--                         FOR SYSTEM_TIME ALL WHERE product_id = ?
-- Fallback documented for the API: rows start when this feature was deployed,
-- so for times before that, the endpoint falls back to the current
-- products.price (no invented history).
CREATE TABLE IF NOT EXISTS productpricehistory (
	product_id INT NOT NULL,
	price      DECIMAL(10,2) NOT NULL,
	PRIMARY KEY (product_id)
) ENGINE = InnoDB
  WITH SYSTEM VERSIONING;

-- seed current prices as the first version (one-time; guarded per product by
-- NOT EXISTS so an accidental re-run does not duplicate rows)
INSERT INTO productpricehistory (product_id, price)
SELECT p.id, p.price FROM products p
WHERE NOT EXISTS (SELECT 1 FROM productpricehistory h WHERE h.product_id = p.id);

DELIMITER //

CREATE TRIGGER IF NOT EXISTS trg_product_price_init
AFTER INSERT ON products
FOR EACH ROW
BEGIN
	INSERT INTO productpricehistory (product_id, price) VALUES (NEW.id, NEW.price);
END //

CREATE TRIGGER IF NOT EXISTS trg_product_price_history
AFTER UPDATE ON products
FOR EACH ROW
BEGIN
	-- only when the price really changed (not on stock_quantity etc.)
	IF NOT (OLD.price <=> NEW.price) THEN
		-- system versioning keeps the superseded version automatically
		UPDATE productpricehistory SET price = NEW.price WHERE product_id = NEW.id;
	END IF;
END //

-- ------------------------------------------------------------- order status log
-- Plain append-only log of status transitions (not versioned: it IS history).
CREATE TABLE IF NOT EXISTS orderstatuslog (
	log_id     BIGINT NOT NULL AUTO_INCREMENT,
	order_id   INT NOT NULL,
	old_status VARCHAR(50) DEFAULT NULL,
	new_status VARCHAR(50) NOT NULL,
	changed_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
	PRIMARY KEY (log_id),
	KEY ix_orderstatuslog_order (order_id, changed_at)
) ENGINE = InnoDB;

CREATE TRIGGER IF NOT EXISTS trg_order_status_log
AFTER UPDATE ON orders
FOR EACH ROW
BEGIN
	IF NOT (OLD.status <=> NEW.status) THEN
		INSERT INTO orderstatuslog (order_id, old_status, new_status)
		VALUES (NEW.id, OLD.status, NEW.status);
	END IF;
END //

DELIMITER ;
