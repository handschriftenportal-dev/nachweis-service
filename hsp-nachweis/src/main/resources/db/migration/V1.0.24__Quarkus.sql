ALTER TABLE beschreibung_bearbeiterids
    DROP CONSTRAINT IF EXISTS beschreibungsdokument_bearbeiter_pkey,
    ADD PRIMARY KEY (beschreibung_id, bearbeiterids);
