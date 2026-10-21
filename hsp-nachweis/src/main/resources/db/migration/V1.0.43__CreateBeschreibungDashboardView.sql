CREATE OR REPLACE VIEW beschreibung_dashboard_view AS
SELECT beschreibung.id                                                 AS hsp_id,
       beschreibung.kodid                                              AS kod_id,
       beschreibung.katalogid                                          AS katalog_id,
       identifikation.ident                                            AS gueltige_signatur,
       beschreibungskomponentekopf.titel                               AS titel,
       beschreibung.verwaltungstyp                                     AS verwaltungstyp,
       beschreibung.dokumentobjekttyp                                  AS dokumentobjekttyp,
       CASE
           WHEN 'HSP_DESCRIPTION_RETRO' = beschreibung.dokumentobjekttyp THEN 'ja|yes'
           ELSE 'nein|no'
           END                                                         AS dokumentobjekttypforfilter,
       author.autoren                                                  AS autoren,
       aufbewahrungsort.name                                           AS aufbewahrungsort,
       besitzer.name                                                   AS besitzer,
       beschreibung.erstellungsdatum                                   AS erstellungsdatum,
       TO_CHAR(beschreibung.erstellungsdatum, 'DD.MM.YYYY HH24:MI:SS') AS erstellungsdatum_string,
       beschreibung.aenderungsdatum                                    AS aenderungsdatum,
       TO_CHAR(beschreibung.aenderungsdatum, 'DD.MM.YYYY HH24:MI:SS')  AS aenderungsdatum_string,
       bearbeiter.bearbeiter_list                                      AS bearbeiter
FROM beschreibungsdokument beschreibung
         LEFT JOIN beschreibungsdokument_globalebeschreibungskomponente bd_gbk
                   ON beschreibung.id = bd_gbk.beschreibung_id
         LEFT JOIN globalebeschreibungskomponente
                   ON bd_gbk.beschreibungsstruktur_id = globalebeschreibungskomponente.id
         LEFT JOIN beschreibungskomponentekopf
                   ON globalebeschreibungskomponente.id = beschreibungskomponentekopf.id
         INNER JOIN identifikation
                    ON beschreibung.gueltigeidentifikation_id = identifikation.id
                        AND identifikation.identtyp = 'GUELTIGE_SIGNATUR'
         LEFT JOIN normdatenreferenz besitzer
                   ON identifikation.besitzer_id = besitzer.id
         LEFT JOIN normdatenreferenz aufbewahrungsort
                   ON identifikation.aufbewahrungsort_id = aufbewahrungsort.id
         LEFT JOIN (SELECT ba.beschreibung_id        AS bsid,
                           string_agg(aut.name, '|') AS autoren
                    FROM beschreibungsdokument_autoren ba
                             LEFT JOIN normdatenreferenz aut
                                       ON ba.autoren_id = aut.id
                    GROUP BY bsid) author
                   ON beschreibung.id = author.bsid
         LEFT JOIN (SELECT bbid.beschreibung_id                              AS bbids,
                           string_agg(b.nachname || ', ' || b.vorname, '; ') AS bearbeiter_list
                    FROM beschreibung_bearbeiterids bbid
                             LEFT JOIN bearbeiter b
                                       ON bbid.bearbeiterids = b.id
                    GROUP BY bbids) bearbeiter
                   ON beschreibung.id = bearbeiter.bbids;

-- Indexes for dashboard and list page queries
CREATE INDEX IF NOT EXISTS idx_beschreibung_aenderungsdatum
    ON beschreibungsdokument (aenderungsdatum DESC NULLS LAST);

CREATE INDEX IF NOT EXISTS idx_bd_gbk_beschreibung_id
    ON beschreibungsdokument_globalebeschreibungskomponente (beschreibung_id);

CREATE INDEX IF NOT EXISTS idx_bd_autoren_beschreibung_id
    ON beschreibungsdokument_autoren (beschreibung_id);

CREATE INDEX IF NOT EXISTS idx_bb_bearbeiterids_beschreibung_id
    ON beschreibung_bearbeiterids (beschreibung_id);
