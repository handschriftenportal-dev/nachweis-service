ALTER TABLE IF EXISTS identifikation
    ADD COLUMN IF NOT EXISTS sort_key VARCHAR(255);

CREATE INDEX IF NOT EXISTS idx_identifikation_sort_key
    ON identifikation (sort_key);