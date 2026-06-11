-- Flyway migration: backfill missing merchant_level and fee_rate in service_fee
-- 1) Fill merchant_level from merchant table when service_fee.merchant_level is NULL or 0
UPDATE service_fee sf
JOIN merchant m ON sf.merchant_id = m.id
SET sf.merchant_level = m.merchant_level
WHERE (sf.merchant_level IS NULL OR sf.merchant_level = 0) AND m.merchant_level IS NOT NULL;

-- 2) Fill fee_rate for records where it's NULL or 0 using fee_amount/order_amount when possible
UPDATE service_fee
SET fee_rate = CASE
  WHEN order_amount IS NOT NULL AND order_amount <> 0 THEN ROUND(fee_amount / order_amount, 6)
  ELSE fee_rate
END
WHERE (fee_rate IS NULL OR fee_rate = 0) AND order_amount IS NOT NULL AND order_amount <> 0;

-- Note: round to 6 decimal places to preserve precision (e.g., 0.001 for 0.1%)

