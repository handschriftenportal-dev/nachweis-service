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
