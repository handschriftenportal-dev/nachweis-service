create table if not exists beschreibungsdokument_autoren
(
    beschreibung_id varchar(255) not null
        constraint fk_beschreibung_autoren
            references beschreibungsdokument,
    autoren_id      varchar(255) not null
        constraint fk_autoren_beschreibung
            references normdatenreferenz,
    constraint beschreibungsdokument_autoren_pkey
        primary key (beschreibung_id, autoren_id)
);
