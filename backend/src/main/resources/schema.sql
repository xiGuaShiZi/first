-- ============================================================
-- 数据库 Schema 变更脚本（幂等，可重复执行）
-- 配合 spring.jpa.defer-datasource-initialization=true 使用
-- 在 Hibernate 初始化之前执行，continue-on-error=true 保证幂等性
-- ============================================================

-- 1. product_review 表：添加 service_attitude_rating 列（商家服务态度评分）
ALTER TABLE product_review ADD COLUMN service_attitude_rating INT NULL COMMENT '商家服务态度评分（1-5）';

-- 2. merchant_wallet 表：添加 pending_balance 列（中间账户待结算金额）
ALTER TABLE merchant_wallet ADD COLUMN pending_balance DECIMAL(19,2) NOT NULL DEFAULT 0 COMMENT '中间账户待结算金额（买家已付款但尚未确认收货）';

-- 3. service_fee 表：添加 remark 列（备注）
ALTER TABLE service_fee ADD COLUMN remark VARCHAR(255) NULL COMMENT '备注（特殊行业适用、临时优惠等扩展信息）';

-- 4. merchant 表：merchant_level 由 VARCHAR 改为 INT（1-5级）
--    先删除旧列，再添加新列（已有商家的等级将重置为默认值1）
ALTER TABLE merchant DROP COLUMN merchant_level;
ALTER TABLE merchant ADD COLUMN merchant_level INT DEFAULT 1 COMMENT '商家等级（1-5级，对应费率0.1%/0.2%/0.5%/0.75%/1%）';

-- 5. service_fee 表：merchant_level 由 VARCHAR 改为 INT（1-5级）
ALTER TABLE service_fee DROP COLUMN merchant_level;
ALTER TABLE service_fee ADD COLUMN merchant_level INT NULL COMMENT '商家等级（1-5级，决定费率）';

-- 6. 新表：buyer_review_by_merchant（商家对买家的评价）
CREATE TABLE IF NOT EXISTS buyer_review_by_merchant (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  order_no VARCHAR(40),
  merchant_id BIGINT NOT NULL,
  merchant_name VARCHAR(128),
  buyer_id BIGINT NOT NULL,
  buyer_name VARCHAR(128),
  rating TINYINT NOT NULL,
  review_type VARCHAR(40) NOT NULL DEFAULT 'PURCHASE',
  content TEXT,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_buyer_review_order_merchant (order_id, merchant_id),
  INDEX idx_buyer_id (buyer_id),
  INDEX idx_merchant_id (merchant_id)
);

