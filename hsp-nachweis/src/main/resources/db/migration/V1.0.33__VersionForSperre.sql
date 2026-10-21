ALTER TABLE sperre ADD COLUMN IF NOT EXISTS version int null;
ALTER TABLE sperre_eintrag ADD COLUMN IF NOT EXISTS version int null;