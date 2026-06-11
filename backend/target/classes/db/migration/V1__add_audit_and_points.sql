-- Flyway migration: add audit and points columns to customer and product if missing
-- Uses INFORMATION_SCHEMA checks so it's safe to run against existing databases

-- NOTE: Flyway will run this on application startup. Keep this file idempotent.

-- customer.points
SET @exists := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'customer'
    AND COLUMN_NAME = 'points'
);
SET @sql := IF(
  @exists = 0,
  "ALTER TABLE `customer` ADD COLUMN `points` INT NOT NULL DEFAULT 0 AFTER `balance`",
  "SELECT 'column points already exists'"
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- customer.audit_status
SET @exists := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'customer'
    AND COLUMN_NAME = 'audit_status'
);
SET @sql := IF(
  @exists = 0,
  "ALTER TABLE `customer` ADD COLUMN `audit_status` INT NOT NULL DEFAULT 1 AFTER `status`",
  "SELECT 'column audit_status already exists'"
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- customer.audit_remark
SET @exists := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'customer'
    AND COLUMN_NAME = 'audit_remark'
);
SET @sql := IF(
  @exists = 0,
  "ALTER TABLE `customer` ADD COLUMN `audit_remark` VARCHAR(255) DEFAULT NULL AFTER `audit_status`",
  "SELECT 'column audit_remark already exists'"
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- customer.audit_time
SET @exists := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'customer'
    AND COLUMN_NAME = 'audit_time'
);
SET @sql := IF(
  @exists = 0,
  "ALTER TABLE `customer` ADD COLUMN `audit_time` DATETIME DEFAULT NULL AFTER `audit_remark`",
  "SELECT 'column audit_time already exists'"
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- product.audit_status
SET @exists := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'product'
    AND COLUMN_NAME = 'audit_status'
);
SET @sql := IF(
  @exists = 0,
  "ALTER TABLE `product` ADD COLUMN `audit_status` INT NOT NULL DEFAULT 1 AFTER `status`",
  "SELECT 'column audit_status already exists in product'"
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- product.audit_remark
SET @exists := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'product'
    AND COLUMN_NAME = 'audit_remark'
);
SET @sql := IF(
  @exists = 0,
  "ALTER TABLE `product` ADD COLUMN `audit_remark` VARCHAR(255) DEFAULT NULL AFTER `audit_status`",
  "SELECT 'column audit_remark already exists in product'"
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- product.audit_time
SET @exists := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'product'
    AND COLUMN_NAME = 'audit_time'
);
SET @sql := IF(
  @exists = 0,
  "ALTER TABLE `product` ADD COLUMN `audit_time` DATETIME DEFAULT NULL AFTER `audit_remark`",
  "SELECT 'column audit_time already exists in product'"
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

