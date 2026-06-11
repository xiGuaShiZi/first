-- Flyway migration: create table to store bargain offers (买家议价出价)
-- Idempotent: check information_schema before creating
SET @exists := (
  SELECT COUNT(*) FROM information_schema.TABLES
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'bargain_offer'
);
SET @sql := IF(@exists = 0,
  'CREATE TABLE `bargain_offer` (
     `id` BIGINT NOT NULL AUTO_INCREMENT,
     `product_id` BIGINT NOT NULL,
     `product_name` VARCHAR(200),
     `buyer_id` BIGINT NOT NULL,
     `buyer_name` VARCHAR(128),
     `merchant_id` BIGINT NOT NULL,
     `merchant_name` VARCHAR(128),
     `original_price` DECIMAL(10,2),
     `offer_price` DECIMAL(10,2) NOT NULL,
     `status` VARCHAR(20) NOT NULL DEFAULT "PENDING" COMMENT "PENDING-待处理，ACCEPTED-已接受，REJECTED-已拒绝，CANCELLED-已取消",
     `reply` TEXT,
     `handle_time` DATETIME,
     `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
     PRIMARY KEY (`id`),
     INDEX `idx_product_id` (`product_id`),
     INDEX `idx_buyer_id` (`buyer_id`),
     INDEX `idx_merchant_id` (`merchant_id`)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;'
  , 'SELECT "table bargain_offer already exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
