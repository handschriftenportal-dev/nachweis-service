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

create table if not exists globalebeschreibungskomponente
(
    id                        varchar(255) not null
        constraint globalebeschreibungskomponente_pkey
            primary key,
    beschreibungsfreitext     varchar(255),
    kulturobjektkomponentetyp varchar(255)
);

create table if not exists beschreibungskomponentekopf
(
    kulturobjekttyp varchar(255),
    status          integer,
    titel           varchar(4096),
    id              varchar(255) not null
        constraint beschreibungskomponentekopf_pkey
            primary key
        constraint fkjtwa6jnucrucsaeswsxg2c237
            references globalebeschreibungskomponente
);

create table if not exists indexjobstatus
(
    id                 bigint not null
        constraint indexjobstatus_pkey
            primary key,
    statusinpercentage varchar(255)
);

create table if not exists normdatenreferenz
(
    id    varchar(255) not null
        constraint normdatenreferenz_pkey
            primary key,
    gndid varchar(255),
    name  varchar(1024)
);

create table if not exists beschreibungsdokument
(
    id                      varchar(255) not null
        constraint beschreibungsdokument_pkey
            primary key,
    aenderungsdatum         timestamp,
    beschreibungstyp        varchar(25),
    erstellungsdatum        timestamp,
    katalogid               varchar(255),
    kodid                   varchar(255),
    teixml                  text,
    verwaltungstyp          varchar(25),
    beschreibungssprache_id varchar(255)
        constraint fkm8wnvnqmn516vx1g4sug51l9n
            references normdatenreferenz
);

create table if not exists beschreibung_altidentifier
(
    beschreibung_id varchar(255) not null
        constraint fkg83o3fl0h79tsayfy6krb3652
            references beschreibungsdokument,
    altidentifier   varchar(255)
);

create table if not exists beschreibungsdokument_globalebeschreibungskomponente
(
    beschreibung_id          varchar(255) not null
        constraint fk4y78hqgdm73bkk68ucmulp741
            references beschreibungsdokument,
    beschreibungsstruktur_id varchar(255) not null
        constraint uk_9cyiixfcfv0pux7kn87imrhmi
            unique
        constraint fk6kqr1lm2it0hx4j9do68ad17q
            references globalebeschreibungskomponente,
    constraint beschreibungsdokument_globalebeschreibungskomponente_pkey
        primary key (beschreibung_id, beschreibungsstruktur_id)
);

create table if not exists beschreibungsdokument_normdatenreferenz
(
    beschreibung_id            varchar(255) not null
        constraint fkk0673th7suuq4f0wdjkqx6iok
            references beschreibungsdokument,
    beschreibungsbeteiligte_id varchar(255) not null
        constraint fk91pi3v2ec07arrp4y20b42b3n
            references normdatenreferenz,
    constraint beschreibungsdokument_normdatenreferenz_pkey
        primary key (beschreibung_id, beschreibungsbeteiligte_id)
);

create table if not exists beschreibungskomponentekopf_grundsprachen
(
    beschreibungskomponentekopf_id varchar(255) not null
        constraint fki0cy3xmjaa2rhev590axqvtf8
            references beschreibungskomponentekopf,
    grundsprachen_id               varchar(255) not null
        constraint fkb1mf63jfnpaodkvv13g12hneb
            references normdatenreferenz,
    constraint beschreibungskomponentekopf_grundsprachen_pkey
        primary key (beschreibungskomponentekopf_id, grundsprachen_id)
);

create table if not exists publikation
(
    id                       varchar(255) not null
        constraint publikation_pkey
            primary key,
    jahrderveroeffentlichung varchar(255)
);

create table if not exists beschreibungsdokument_publikation
(
    beschreibung_id  varchar(255) not null
        constraint fk9hrhndt256os6kjrr1eyh2n5n
            references beschreibungsdokument,
    publikationen_id varchar(255) not null
        constraint uk_8ippa7i2ekm5u9hy148psrchb
            unique
        constraint fkicgu82aym6kstv37gdghfslnt
            references publikation,
    constraint beschreibungsdokument_publikation_pkey
        primary key (beschreibung_id, publikationen_id)
);

create table if not exists referenzquelle
(
    typ   varchar(255) not null
        constraint referenzquelle_pkey
            primary key,
    label varchar(255)
);

create table if not exists stream_actor
(
    id   varchar(255) not null
        constraint stream_actor_pkey
            primary key,
    name varchar(255),
    type varchar(255),
    url  varchar(255)
);

create table if not exists stream_object
(
    id         varchar(255) not null
        constraint stream_object_pkey
            primary key,
    compressed boolean,
    content    bytea,
    groupid    varchar(255),
    mediatype  varchar(255),
    name       varchar(255),
    type       varchar(255),
    url        varchar(255),
    version    varchar(255)
);

create table if not exists stream_object_tag
(
    id   varchar(255) not null
        constraint stream_object_tag_pkey
            primary key,
    name varchar(255),
    type varchar(255)
);

create table if not exists stream_object_stream_object_tag
(
    activitystreamobjectdto_id varchar(255) not null
        constraint fkfducr1k4iiuj939v49e53kvyo
            references stream_object,
    tag_id                     varchar(255) not null
        constraint fkh7bohu025egdvvncy25gpjabx
            references stream_object_tag,
    constraint stream_object_stream_object_tag_pkey
        primary key (activitystreamobjectdto_id, tag_id)
);

create table if not exists stream_target
(
    id   varchar(255) not null
        constraint stream_target_pkey
            primary key,
    name varchar(255),
    type varchar(255)
);

create table if not exists stream_message
(
    id        varchar(255) not null
        constraint stream_message_pkey
            primary key,
    action    varchar(255),
    context   varchar(255),
    published timestamp,
    summary   varchar(255),
    version   varchar(255),
    actor_id  varchar(255)
        constraint fkamousliri6w24po5mekhxeswb
            references stream_actor,
    target_id varchar(255)
        constraint fkmxs9esex1a2h9m5l2o67aj1h7
            references stream_target
);

create table if not exists beschreibungimport
(
    id                  varchar(255) not null
        constraint beschreibungimport_pkey
            primary key,
    creationdate        timestamp,
    fehlerbeschreibung  varchar(255),
    isselectedforimport boolean,
    modificationdate    timestamp,
    status              varchar(15),
    messagedto_id       varchar(255)
        constraint fkrekmbsmom962qm2pi31g1d5bv
            references stream_message
);

create table if not exists digitalisatimport
(
    id            varchar(255) not null
        constraint digitalisatimport_pkey
            primary key,
    errormessage  varchar(255),
    messagedto_id varchar(255)
        constraint fkr7t21xcybokd8ydlurynqwg9r
            references stream_message
);

create table if not exists stream_message_stream_object
(
    activitystreammessagedto_id varchar(255) not null
        constraint fk4d7qd9hvqnu29wfegsa0q7yvf
            references stream_message,
    objects_id                  varchar(255) not null
        constraint uk_f2b084nl830o4f4yslc8s0h74
            unique
        constraint fk7tdiuhbbm4aj1078rc88ifa12
            references stream_object
);

create table if not exists identifikation
(
    id                             varchar(255) not null
        constraint identifikation_pkey
            primary key,
    ident                          varchar(255),
    identtyp                       varchar(25),
    aufbewahrungsort_id            varchar(255)
        constraint fkkihnjv59xfdfxk8fwn7uf37gl
            references normdatenreferenz,
    besitzer_id                    varchar(255)
        constraint fkb6oxvqlwq18lgfsyrxlgq9eg0
            references normdatenreferenz,
    alternativeidentifikationen_id varchar(255)
);

create table if not exists globalebeschreibungskomponente_identifikation
(
    globalebeschreibungskomponente_id varchar(255) not null
        constraint fk3xdsfjyeptxvhplpu024p63j4
            references globalebeschreibungskomponente,
    identifikationen_id               varchar(255) not null
        constraint uk_l3jm4mr9ors681do40iqbt7ei
            unique
        constraint fk3eteoqatiahy3owamubed04rg
            references identifikation,
    constraint globalebeschreibungskomponente_identifikation_pkey
        primary key (globalebeschreibungskomponente_id, identifikationen_id)
);

create table if not exists identifikation_sammlungids
(
    identifikation_id varchar(255) not null
        constraint fkt0m9q688nvy7ux88iim4bllmc
            references identifikation,
    sammlungids       varchar(255)
);

create table if not exists kulturobjektdokument
(
    dtype                     varchar(31)  not null,
    id                        varchar(255) not null
        constraint kulturobjektdokument_pkey
            primary key,
    gndidentifier             varchar(255),
    registrierungsdatum       timestamp,
    teixml                    text,
    gueltigeidentifikation_id varchar(255)
        constraint fk4gd8k7k9fabkgp4phowplecog
            references identifikation,
    komponenten_id            varchar(255)
        constraint fkdevq7uemvmy1to15v0rtvb6g5
            references kulturobjektdokument
);

create table if not exists digitalisat
(
    id                             varchar(255) not null
        constraint digitalisat_pkey
            primary key,
    alternativeurl                 varchar(255),
    digitalisierungsdatum          date,
    erstellungsdatum               date,
    manifesturl                    varchar(255),
    thumbnailurl                   varchar(255),
    typ                            varchar(255),
    digitalisierendeeinrichtung_id varchar(255)
        constraint fkroxwget0ucbt7213ss78a1a72
            references normdatenreferenz,
    entstehungsort_id              varchar(255)
        constraint fk2m5jwvs8seyqlkib4jb18x8iy
            references normdatenreferenz,
    digitalisate_id                varchar(255)
        constraint fknm1vxwmn2ts9etau5c8m343em
            references kulturobjektdokument
);

alter table identifikation
    add constraint fkj5oh11y2b2uo50ltmqtgld8m7
        foreign key (alternativeidentifikationen_id) references kulturobjektdokument;

create table if not exists kulturobjektdokument_beschreibungenids
(
    kulturobjektdokument_id varchar(255) not null
        constraint fk4r2dnyohqcp6aadg2iyxsu34n
            references kulturobjektdokument,
    beschreibungenids       varchar(255)
);

create table if not exists referenz
(
    id                   varchar(255) not null
        constraint referenz_pkey
            primary key,
    typ                  varchar(15),
    url                  varchar(255),
    referenzquelle_typ   varchar(255)
        constraint fk9ry2qghuor8imw0p2avgteb75
            references referenzquelle,
    externereferenzen_id varchar(255)
        constraint fkpspkkm8k1gpw83wo4tha3peq9
            references kulturobjektdokument
);

create table if not exists beschreibungsdokument_referenz
(
    beschreibung_id varchar(255) not null
        constraint fkhje634tusggvxli2559llxc4o
            references beschreibungsdokument,
    externeids_id   varchar(255) not null
        constraint uk_490qrufc5vbv315pcmhukc870
            unique
        constraint fkgh581vwj3vfqig5onvhm8us3m
            references referenz,
    constraint beschreibungsdokument_referenz_pkey
        primary key (beschreibung_id, externeids_id)
);
