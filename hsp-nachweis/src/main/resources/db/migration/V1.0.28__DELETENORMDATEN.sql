BEGIN TRANSACTION;
ALTER TABLE identifikation
    DROP CONSTRAINT fkb6oxvqlwq18lgfsyrxlgq9eg0;
ALTER TABLE identifikation
    DROP CONSTRAINT fkkihnjv59xfdfxk8fwn7uf37gl;
ALTER TABLE digitalisat
    DROP CONSTRAINT fk2m5jwvs8seyqlkib4jb18x8iy;
ALTER TABLE beschreibungsdokument_normdatenreferenz
    DROP CONSTRAINT fk91pi3v2ec07arrp4y20b42b3n;
ALTER TABLE katalog_autoren
    DROP CONSTRAINT fk_katalog_autoren;
ALTER TABLE katalog
    DROP CONSTRAINT fk_katalog_verlag;
ALTER TABLE bearbeiter
    DROP CONSTRAINT fk_normdatenreferenz_bearbeiter;
ALTER TABLE bearbeiter
    DROP CONSTRAINT fk_normdatenreferenz_bearbeiter_person;
ALTER TABLE normdatenreferenz_identifikator
    DROP CONSTRAINT fk_nr_identifikator_normdatenreferenz;
ALTER TABLE normdatenreferenz_variantername
    DROP CONSTRAINT fk_nr_variantername_normdatenreferenz;
ALTER TABLE beschreibungskomponentekopf_grundsprachen
    DROP CONSTRAINT fkb1mf63jfnpaodkvv13g12hneb;
ALTER TABLE digitalisat_normdatenreferenz
    DROP CONSTRAINT fkgh581vwj3vfqig5onvhm8us3k;
ALTER TABLE beschreibungsdokument
    DROP CONSTRAINT fkm8wnvnqmn516vx1g4sug51l9n;
ALTER TABLE digitalisat
    DROP CONSTRAINT fkroxwget0ucbt7213ss78a1a72;
ALTER TABLE beschreibungsdokument_autoren
    DROP CONSTRAINT fk_autoren_beschreibung;

-- explain (analyze,buffers,timing)
DELETE
FROM katalog_beschreibungs_referenzen;

DELETE
FROM katalog_beschreibung_referenz;

DELETE
FROM katalog_autoren;

DELETE
FROM katalog;

DELETE
FROM beschreibungsdokument_normdatenreferenz
WHERE beschreibungsdokument_normdatenreferenz.beschreibungsbeteiligte_id IN
      (SELECT beschreibungsbeteiligte_id
       from beschreibungsdokument_normdatenreferenz
       WHERE beschreibungsbeteiligte_id not like 'NORM-%');

-- Autoren

DELETE
FROM beschreibungsdokument_autoren
WHERE autoren_id IN (SELECT autoren_id
                     from beschreibungsdokument_autoren
                     WHERE autoren_id not like 'NORM-%');

--- Normdatenreferenz

DELETE
FROM normdatenreferenz
WHERE id IN (SELECT id from normdatenreferenz WHERE id not like 'NORM-%');

ALTER TABLE bearbeiter
    ADD CONSTRAINT fk_normdatenreferenz_bearbeiter FOREIGN KEY (institution_id) REFERENCES normdatenreferenz (id);
ALTER TABLE bearbeiter
    ADD CONSTRAINT fk_normdatenreferenz_bearbeiter_person FOREIGN KEY (person_id) REFERENCES normdatenreferenz (id);
ALTER TABLE beschreibungsdokument
    ADD CONSTRAINT fk_normdatenreferenz_beschreibungssprache FOREIGN KEY (beschreibungssprache_id) REFERENCES normdatenreferenz (id);
ALTER TABLE beschreibungsdokument_autoren
    ADD CONSTRAINT fk_autoren_beschreibung FOREIGN KEY (autoren_id) REFERENCES normdatenreferenz (id);
ALTER TABLE beschreibungsdokument_normdatenreferenz
    ADD CONSTRAINT fk_normdatenreferenz_beschreibungsbeteiligte FOREIGN KEY (beschreibungsbeteiligte_id) REFERENCES normdatenreferenz (id);
ALTER TABLE beschreibungskomponentekopf_grundsprachen
    ADD CONSTRAINT fk_normdatenreferenz_beschreibungsgrundsprache FOREIGN KEY (grundsprachen_id) REFERENCES normdatenreferenz (id);
ALTER TABLE digitalisat
    ADD CONSTRAINT fk_normdatenreferenz_digitalisatentstehungsort FOREIGN KEY (entstehungsort_id) REFERENCES normdatenreferenz (id);
ALTER TABLE digitalisat
    ADD CONSTRAINT fk_normdatenreferenz_digitalisateinrichtung FOREIGN KEY (digitalisierendeeinrichtung_id) REFERENCES normdatenreferenz (id);
ALTER TABLE digitalisat_normdatenreferenz
    ADD CONSTRAINT fk_normdatenreferenz_digitalisatkonzepte FOREIGN KEY (digitalisatskonzepte_id) REFERENCES normdatenreferenz (id);
ALTER TABLE identifikation
    ADD CONSTRAINT fk_normdatenreferenz_identifikationbesitzer FOREIGN KEY (besitzer_id) REFERENCES normdatenreferenz (id);
ALTER TABLE identifikation
    ADD CONSTRAINT fk_normdatenreferenz_identifikationaufbewahrungsort FOREIGN KEY (aufbewahrungsort_id) REFERENCES normdatenreferenz (id);
ALTER TABLE katalog
    ADD CONSTRAINT fk_katalog_verlag FOREIGN KEY (verlag_id) REFERENCES normdatenreferenz (id);
ALTER TABLE katalog_autoren
    ADD CONSTRAINT fk_katalog_autoren FOREIGN KEY (autoren_id) REFERENCES normdatenreferenz (id);
ALTER TABLE normdatenreferenz_identifikator
    ADD CONSTRAINT fk_nr_identifikator_normdatenreferenz FOREIGN KEY (normdatenreferenz_id) REFERENCES normdatenreferenz (id);
ALTER TABLE normdatenreferenz_variantername
    ADD CONSTRAINT fk_nr_variantername_normdatenreferenz FOREIGN KEY (normdatenreferenz_id) REFERENCES normdatenreferenz (id);

COMMIT TRANSACTION;
END TRANSACTION;