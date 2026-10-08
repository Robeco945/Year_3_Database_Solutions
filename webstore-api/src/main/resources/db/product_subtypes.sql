-- webstore-api: schema change for the inheritance case (PROJECT_PLAN.md §3)
-- Product  ->  PhysicalProduct / DigitalProduct (JPA JOINED strategy).
--
-- The provided webstore schema has no discriminator or subclass tables, so this
-- (documented, small) schema change adds:
--   1. a discriminator column `dtype` on products
--   2. two subclass tables joined on the products primary key
-- Existing products are base-class products (dtype = 'P', the default).
--
-- Run ONCE as a DBA/admin account, before the application starts
-- (Hibernate runs with ddl-auto: validate):
--   mariadb -h 127.0.0.1 -u root -p < webstore-api/src/main/resources/db/product_subtypes.sql

USE webstore;

-- 1) discriminator column; every existing product becomes a base-class product
ALTER TABLE products
	ADD COLUMN IF NOT EXISTS dtype VARCHAR(31) NOT NULL DEFAULT 'P';

-- 2) subclass tables. PK mirrors products.id (uniqueness 1:1 with parent row)
CREATE TABLE IF NOT EXISTS physicalproducts (
	product_id INT NOT NULL,
	weight_grams INT DEFAULT NULL,
	PRIMARY KEY (product_id),
	CONSTRAINT fk_physicalproduct_product FOREIGN KEY (product_id)
		REFERENCES products (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS digitalproducts (
	product_id INT NOT NULL,
	download_url VARCHAR(255) DEFAULT NULL,
	PRIMARY KEY (product_id),
	CONSTRAINT fk_digitalproduct_product FOREIGN KEY (product_id)
		REFERENCES products (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
