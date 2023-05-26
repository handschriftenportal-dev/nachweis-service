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
