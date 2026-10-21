ALTER TABLE beschreibung_altidentifier
DROP CONSTRAINT fkg83o3fl0h79tsayfy6krb3652;

ALTER TABLE beschreibung_altidentifier
ADD CONSTRAINT fkg83o3fl0h79tsayfy6krb3652
FOREIGN KEY (beschreibung_id) REFERENCES beschreibungsdokument(id) ON DELETE CASCADE;

ALTER TABLE beschreibung_bearbeiterids
DROP CONSTRAINT fk_beschreibung_bearbeiterids;

ALTER TABLE beschreibung_bearbeiterids
ADD CONSTRAINT fk_beschreibung_bearbeiterids
FOREIGN KEY (beschreibung_id) REFERENCES beschreibungsdokument(id) ON DELETE CASCADE;
