-- Ticket 23861: TEI-Stand zum Publikationszeitpunkt
-- Stores the TEI-XML of a Beschreibung as a ZIP, captured when it's published.
-- Own table so the blob is fetched only on explicit demand (download), never with
-- every Publikation load. The Publikation entity exposes a read-only @Formula-derived
-- boolean (EXISTS subquery) so the UI can gate the download button.

CREATE TABLE IF NOT EXISTS publikation_tei
(
    publikation_id VARCHAR(255) NOT NULL PRIMARY KEY,
    tei_zip        BYTEA        NOT NULL,
    captured_at    TIMESTAMP    NOT NULL,
    CONSTRAINT fk_publikation_tei_publikation
        FOREIGN KEY (publikation_id) REFERENCES Publikation (id) ON DELETE CASCADE
);
