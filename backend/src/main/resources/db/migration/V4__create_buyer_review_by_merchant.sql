-- Flyway migration: create table to store merchant->buyer reviews (商家对买家的评价)
-- Idempotent: check information_schema before creating
SET @exists := (
  SELECT COUNT(*) FROM information_schema.TABLES
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'buyer_review_by_merchant'
);
SET @sql := IF(@exists = 0,
  'CREATE TABLE `buyer_review_by_merchant` (
     `id` BIGINT NOT NULL AUTO_INCREMENT,
     `order_id` BIGINT NOT NULL,
     `order_no` VARCHAR(40),
     `merchant_id` BIGINT NOT NULL,
     `merchant_name` VARCHAR(128),
     `buyer_id` BIGINT NOT NULL,
     `buyer_name` VARCHAR(128),
     `rating` TINYINT NOT NULL COMMENT "商家对买家的评分，1-5",
     `review_type` VARCHAR(40) NOT NULL DEFAULT "PURCHASE",
     `content` TEXT,
     `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
     PRIMARY KEY (`id`),
     UNIQUE KEY `uk_buyer_review_order_merchant` (`order_id`, `merchant_id`),
     INDEX `idx_buyer_id` (`buyer_id`),
     INDEX `idx_merchant_id` (`merchant_id`)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;'
  , 'SELECT "table buyer_review_by_merchant already exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

