alter table if exists beschreibungsdokument
    alter column teixml set data type TEXT;
alter table if exists geloeschtesdokument
    alter column id set data type varchar(255);
alter table if exists geloeschtesdokument
    alter column tei_xml set data type TEXT;
alter table if exists Katalog
    alter column tei_xml set data type TEXT;
alter table if exists KulturObjektDokument
    alter column teiXML set data type TEXT;