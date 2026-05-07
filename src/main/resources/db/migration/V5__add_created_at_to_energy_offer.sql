-- Add created_at column to energy_offer table
ALTER TABLE energy_offer ADD COLUMN IF NOT EXISTS created_at TIMESTAMP;

-- Backfill existing offers with current timestamp as approximation
UPDATE energy_offer SET created_at = NOW() WHERE created_at IS NULL;
