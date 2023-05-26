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

alter table normdatenreferenz
    add if not exists typename varchar (255);

create table if not exists normdatenreferenz_identifikator
(
    normdatenreferenz_id varchar
(
    255
) not null
    constraint fk_nr_identifikator_normdatenreferenz
    references normdatenreferenz,
    text varchar
(
    1024
) not null,
    type varchar
(
    255
) not null,
    url varchar
(
    1024
)
    );

create table if not exists normdatenreferenz_variantername
(
    normdatenreferenz_id varchar(255)  not null
        constraint fk_nr_variantername_normdatenreferenz
            references normdatenreferenz,
    languagecode         varchar(6),
    name                 varchar(1024) not null
);