create table if not exists geloeschtesdokument
(
    id                     varchar(64)   not null
        constraint geloeschtesdokument_pkey
            primary key,
    dokument_id            varchar(1024) not null,
    dokument_objekt_typ    varchar(64)   not null,
    gueltige_signatur      varchar(1024),
    alternative_signaturen varchar(4098),
    interne_purls          varchar(4098),
    besitzer_id            varchar(64)   not null,
    besitzer_name          varchar(1024) not null,
    aufbewahrungsort_id    varchar(64)   not null,
    aufbewahrungsort_name  varchar(1024) not null,
    tei_xml                xml,
    loeschdatum            timestamp     not null,
    bearbeiter_id          varchar(64)   not null,
    bearbeiter_name        varchar(1024) not null
)
