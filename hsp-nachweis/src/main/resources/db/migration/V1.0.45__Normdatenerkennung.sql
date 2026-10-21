CREATE TABLE IF NOT EXISTS normdaten_erkennung_auftrag
(
    id                       VARCHAR(64) PRIMARY KEY,
    beschreibungsdokument_id VARCHAR(255) NOT NULL REFERENCES beschreibungsdokument (id) ON DELETE CASCADE,
    status                   VARCHAR(32)  NOT NULL,
    bearbeiter_id            VARCHAR(64) REFERENCES bearbeiter (id) ON DELETE NO ACTION,
    applied_by_id            VARCHAR(64) REFERENCES bearbeiter (id) ON DELETE NO ACTION,
    created_at               TIMESTAMP    NOT NULL,
    updated_at               TIMESTAMP,
    error_message            TEXT
);

CREATE TABLE IF NOT EXISTS normdaten_erkennung_eintrag
(
    id                   VARCHAR(64) PRIMARY KEY,
    request_id           VARCHAR(64) NOT NULL REFERENCES normdaten_erkennung_auftrag (id) ON DELETE CASCADE,
    xpath                TEXT        NOT NULL,
    normdaten_typ        VARCHAR(32) NOT NULL,
    display_text         TEXT        NOT NULL,
    request_params       JSONB,
    response             JSONB,
    error_message        TEXT,
    selection_type       VARCHAR(32) NOT NULL DEFAULT 'NONE',
    selected_match_index INTEGER,
    manual_identifier    VARCHAR(128),
    reviewed_by_id       VARCHAR(64) REFERENCES bearbeiter (id) ON DELETE NO ACTION,
    created_at           TIMESTAMP   NOT NULL,
    updated_at           TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_request_beschreibungsdokument_id on normdaten_erkennung_auftrag (beschreibungsdokument_id);
CREATE INDEX IF NOT EXISTS idx_request_status on normdaten_erkennung_auftrag (status);
CREATE INDEX IF NOT EXISTS idx_response_request_id on normdaten_erkennung_eintrag (request_id);
