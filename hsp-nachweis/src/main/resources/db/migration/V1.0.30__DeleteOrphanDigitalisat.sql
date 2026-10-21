DELETE
FROM digitalisat
WHERE digitalisate_id IS NULL
  AND digitalisat.id NOT IN (SELECT digitalisat_id FROM katalog);