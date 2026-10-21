create table if not exists beschreibung_bearbeiterids
(
    beschreibung_id varchar(255) not null
    constraint fk_beschreibung_bearbeiterids
    references beschreibungsdokument,
    bearbeiterids varchar(255)
    constraint fk_bearbeiter_beschreibung
    references bearbeiter,
    constraint beschreibungsdokument_bearbeiter_pkey
    primary key (beschreibung_id)
    );