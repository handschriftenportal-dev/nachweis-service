alter table beschreibungsdokument
    drop column if exists lizenztyp;

create table if not exists lizenz
(
    id                 varchar(255) not null
        constraint lizenz_pkey
            primary key,
    beschreibungs_text text
);

create table if not exists lizenz_uris
(
    lizenz_id varchar(255) not null
        constraint fka159javplom8uwbi2ud3fdv1a
            references lizenz,
    uris      varchar(255)
);

alter table beschreibungsdokument
    ADD if not exists
        lizenz_id varchar(255)
            constraint fklx9g8sp7oq8dbpcmq6ty9nq0r
                references lizenz;

