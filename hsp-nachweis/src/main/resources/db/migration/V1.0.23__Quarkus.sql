CREATE TABLE IF NOT EXISTS datensicherung
(
    id         varchar(255) primary key,
    start_zeit timestamp not null,
    end_zeit   timestamp,
    status     varchar(255),
    datei_name varchar(255),
    benutzer   varchar(255)
);

CREATE TABLE IF NOT EXISTS datensicherung_dokumentauswahl
(
    datensicherung_id varchar(255) not null references datensicherung (id),
    dokument_auswahl  varchar(255) not null,
    primary key (datensicherung_id, dokument_auswahl)
);