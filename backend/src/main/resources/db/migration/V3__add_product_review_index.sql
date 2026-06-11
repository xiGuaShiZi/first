-- Flyway migration: add index on product_review.product_id to speed up aggregations
-- Idempotent: checks information_schema.statistics before adding

SET @exists := (
  SELECT COUNT(*) FROM information_schema.statistics
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'product_review'
    AND INDEX_NAME = 'idx_product_review_product_id'
);
SET @sql := IF(
  @exists = 0,
  'ALTER TABLE `product_review` ADD INDEX `idx_product_review_product_id` (`product_id`);',
  'SELECT "index idx_product_review_product_id already exists"'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

