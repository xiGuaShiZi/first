-- Flyway migration: create merchant_level_audit table to record merchant level changes
CREATE TABLE IF NOT EXISTS `merchant_level_audit` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `merchant_id` BIGINT NOT NULL,
  `operator_id` BIGINT DEFAULT NULL,
  `operator_name` VARCHAR(128) DEFAULT NULL,
  `old_level` INT DEFAULT NULL,
  `new_level` INT DEFAULT NULL,
  `reason` VARCHAR(255) DEFAULT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_merchant_id` (`merchant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

