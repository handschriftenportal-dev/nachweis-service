alter table katalog
    alter column title type varchar(4096) using title::varchar(4096);

alter table kulturobjektdokument
    alter column teixml type xml using teixml::xml;

alter table beschreibungsdokument
    alter column teixml type xml using teixml::xml;

alter table katalog
    alter column tei_xml type xml using tei_xml::xml;
