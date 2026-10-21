-- Zuerst die Referenzen löschen
DELETE
FROM digitalisat_normdatenreferenz
WHERE digitalisat_id IN (SELECT id
                         FROM digitalisat
                         WHERE id NOT IN (SELECT digitalisat_id
                                          FROM kulturobjektdokument_digitalisate)
                           AND id NOT IN (SELECT digitalisat_id
                                          FROM katalog));

-- Dann die Digitalisate löschen
DELETE
FROM digitalisat
WHERE id NOT IN (SELECT digitalisat_id
                 FROM kulturobjektdokument_digitalisate)
  AND id NOT IN (SELECT digitalisat_id
                 FROM katalog);
