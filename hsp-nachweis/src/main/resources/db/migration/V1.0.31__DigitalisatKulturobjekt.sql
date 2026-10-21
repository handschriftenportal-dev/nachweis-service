-- 1. Erstellen der neuen Join-Tabelle
CREATE TABLE kulturobjektdokument_digitalisate
(
    kulturobjektdokument_id varchar(255) not null,
    digitalisat_id          varchar(255) not null,
    PRIMARY KEY (kulturobjektdokument_id, digitalisat_id),
    CONSTRAINT fk_kulturobjekt FOREIGN KEY (kulturobjektdokument_id) REFERENCES kulturobjektdokument (id) ON DELETE CASCADE,
    CONSTRAINT fk_digitalisat FOREIGN KEY (digitalisat_id) REFERENCES digitalisat (id) ON DELETE CASCADE
);

-- 2. Datenmigration von der alten Struktur in die neue

INSERT INTO kulturobjektdokument_digitalisate (kulturobjektdokument_id, digitalisat_id)
SELECT digitalisate_id, id
FROM digitalisat
WHERE digitalisate_id IS NOT NULL;

-- 3. (Optional) Entfernen der alten Fremdschlüsselspalte aus Tabelle b
ALTER TABLE digitalisat
    DROP COLUMN digitalisate_id;
