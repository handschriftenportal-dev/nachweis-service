create table if not exists konfiguration
(
    schluessel varchar(1024) not null
        constraint konfiguration_pkey
            primary key,
    wert       varchar(1024) not null
);
