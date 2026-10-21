-- Extend beschreibung_dashboard_view with publikation fields required by
-- the Beschreibungen filter list (ticket 23971). Additive change: new columns
-- are appended at the end so dependent views based on explicit column lists
-- continue to work unchanged.

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
       bearbeiter.bearbeiter_list                                      AS bearbeiter,
       p.publikations_datum_hsp                                        AS publikations_datum_hsp,
       p.status                                                        AS publikation_status,
       internal_purl.purl                                              AS hsp_purl
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
                   ON beschreibung.id = bearbeiter.bbids
         LEFT JOIN publikation p
                   ON beschreibung.publikation_id = p.id
         LEFT JOIN (SELECT publikation_id AS pid,
                           purl
                    FROM purl
                    WHERE typ = 'INTERNAL') internal_purl
                   ON p.id = internal_purl.pid;

CREATE INDEX IF NOT EXISTS idx_purl_publikation_typ
    ON purl (publikation_id, typ);
