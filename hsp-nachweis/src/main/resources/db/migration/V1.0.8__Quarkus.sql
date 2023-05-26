/*
 * MIT License
 *
 * Copyright (c) 2023 Staatsbibliothek zu Berlin - Preußischer Kulturbesitz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

create table if not exists katalog
(
    id                varchar(255) not null
        primary key,
    aenderungsdatum   timestamp,
    erstelldatum      timestamp,
    hsk_id            varchar(255),
    lizenz_uri        varchar(255),
    publikations_jahr varchar(255),
    tei_xml           text,
    title             varchar(255),
    digitalisat_id    varchar(255)
        constraint fk_katalog_digitalisat
            references digitalisat,
    verlag_id         varchar(255)
        constraint fk_katalog_verlag
            references normdatenreferenz
);

create table if not exists katalog_autoren
(
    katalog_id varchar(255) not null
        constraint fk_katalog_autoren_katalog
            references katalog,
    autoren_id varchar(255) not null
        constraint fk_katalog_autoren
            references normdatenreferenz,
    primary key (katalog_id, autoren_id)
);

create table if not exists katalog_beschreibung_referenz
(
    beschreibungs_id   varchar(255) not null
        primary key,
    manifest_range_url varchar(255),
    ocr_volltext       text,
    seite_bis          varchar(255),
    seite_von          varchar(255)
);

create table if not exists katalog_beschreibungs_referenzen
(
    katalog_id                      varchar(255) not null
        constraint fk_katalog_beschreibungs_referenzen_katalog
            references katalog,
    beschreibungen_beschreibungs_id varchar(255) not null
        constraint uk_8aj0tf0tdoy1g6i9h4osw5h3o
            unique
        constraint fk_katalog_beschreibungs_referenzen
            references katalog_beschreibung_referenz,
    primary key (katalog_id, beschreibungen_beschreibungs_id)
);



