DELETE from beschreibungsdokument b where b.id not in (select k.beschreibungenids from kulturobjektdokument_beschreibungenids k where k.beschreibungenids = b.id);
