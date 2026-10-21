create table if not exists bearbeiter
(
    id             varchar(64) not null
        constraint bearbeiter_pkey
            primary key,
    bearbeitername varchar(64),
    email          varchar(512),
    nachname       varchar(255),
    vorname        varchar(255),
    institution_id varchar(255)
        constraint fk_normdatenreferenz_bearbeiter
            references normdatenreferenz
);

INSERT INTO bearbeiter (id, bearbeitername, email, nachname, vorname, institution_id)
VALUES ('unbekannterBearbeiter', 'unbekannterBearbeiter', null, null, null, null) ON CONFLICT
ON CONSTRAINT bearbeiter_pkey
    DO NOTHING;

create table if not exists sperre
(
    id           varchar(64) not null
        constraint sperre_pkey
            primary key,
    bearbeiter_id  varchar(64)
        constraint fk_sperre_bearbeiter
            references bearbeiter,
    start_datum  timestamp,
    sperre_grund varchar(512),
    sperre_typ   varchar(32),
    tx_id        varchar(64)
);

create table if not exists sperre_eintrag
(
    target_id   varchar(64) not null,
    target_type varchar(32) not null,
    sperre_id   varchar(64)
        constraint fk_sperre_eintrag_sperre
            references sperre on delete cascade,
    constraint sperre_eintrag_pkey
        primary key (target_id, target_type)
);
