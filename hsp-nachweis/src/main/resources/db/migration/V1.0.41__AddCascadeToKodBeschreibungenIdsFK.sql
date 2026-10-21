-- Add ON DELETE CASCADE to kulturobjektdokument_beschreibungenids FK
-- This allows KulturObjektDokument to be deleted without first removing entries from the join table

ALTER TABLE kulturobjektdokument_beschreibungenids
    DROP CONSTRAINT IF EXISTS fk4r2dnyohqcp6aadg2iyxsu34n;

ALTER TABLE kulturobjektdokument_beschreibungenids
    ADD CONSTRAINT fk_kod_beschreibungenids_kod
        FOREIGN KEY (kulturobjektdokument_id)
            REFERENCES kulturobjektdokument (id)
            ON DELETE CASCADE;