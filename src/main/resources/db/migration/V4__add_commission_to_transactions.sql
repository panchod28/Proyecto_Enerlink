-- Add commission column to transactions table
ALTER TABLE transactions ADD COLUMN IF NOT EXISTS commission DOUBLE PRECISION DEFAULT 0.0;

-- Backfill existing transactions with 2% commission of (kwh * price)
UPDATE transactions SET commission = ROUND((kwh * price * 0.02)::numeric, 2)
WHERE commission = 0.0 OR commission IS NULL;
