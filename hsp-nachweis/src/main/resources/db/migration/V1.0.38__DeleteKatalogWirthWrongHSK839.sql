DELETE
FROM katalog_beschreibungs_referenzen kbr
where kbr.katalog_id = 'HSP-8f7d807e-1f53-3ff5-b9ef-be5cb81090fb';

DELETE
FROM katalog_beschreibung_referenz kr
WHERE NOT EXISTS(select 1
                 from katalog_beschreibungs_referenzen kbr
                 WHERE kbr.beschreibungen_beschreibungs_id = kr.beschreibungs_id);

DELETE
FROM katalog_autoren
WHERE katalog_id = 'HSP-8f7d807e-1f53-3ff5-b9ef-be5cb81090fb';

DELETE
FROM katalog
WHERE id = 'HSP-8f7d807e-1f53-3ff5-b9ef-be5cb81090fb'
  AND hsk_id = '839'
  AND publikations_jahr = '2018';

COMMIT TRANSACTION;
END TRANSACTION;
