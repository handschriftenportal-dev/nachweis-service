create table if not exists purl
(
    id                       varchar(255) not null
        constraint purl_pk
            primary key,
    purl                     varchar(255) not null,
    target                   varchar(255) not null,
    typ                      varchar(50)  not null,
    kulturobjektdokument_id  varchar(255)
        constraint purl_kulturobjektdokument_id_fk
            references kulturobjektdokument,
    beschreibungsdokument_id varchar(255)
        constraint purl_beschreibungsdokument_id_fk
            references beschreibungsdokument
);

create unique index if not exists purl_id_uindex
    on purl (id);

create unique index if not exists purl_purl_uindex
    on purl (purl);
