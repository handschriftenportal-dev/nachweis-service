ALTER TABLE beschreibungsdokument
    ADD COLUMN IF NOT EXISTS gueltigeidentifikation_id varchar(255);
ALTER TABLE beschreibungsdokument
    DROP CONSTRAINT IF EXISTS beschreibung_gueltigeidentifikation_fk;
ALTER TABLE beschreibungsdokument
    ADD CONSTRAINT beschreibung_gueltigeidentifikation_fk FOREIGN KEY (gueltigeidentifikation_id) REFERENCES Identifikation (id);

WITH sub AS (SELECT beschreibung.id                                       as beschreibungId,
                    globale_komponente_identifikation.identifikationen_id as identifikation_id
             FROM beschreibungsdokument beschreibung
                      INNER JOIN public.beschreibungsdokument_globalebeschreibungskomponente bg_join
                                 ON beschreibung.id = bg_join.beschreibung_id
                      INNER JOIN globalebeschreibungskomponente globale_komponente
                                 ON bg_join.beschreibungsstruktur_id = globale_komponente.id
                      INNER JOIN globalebeschreibungskomponente_identifikation globale_komponente_identifikation
                                 ON globale_komponente.id =
                                    globale_komponente_identifikation.globalebeschreibungskomponente_id
                      INNER JOIN identifikation
                                 ON globale_komponente_identifikation.identifikationen_id =
                                    identifikation.id
             WHERE identtyp = 'GUELTIGE_SIGNATUR')
UPDATE beschreibungsdokument
SET gueltigeidentifikation_id = sub.identifikation_id
FROM sub
WHERE beschreibungsdokument.id = sub.beschreibungId;