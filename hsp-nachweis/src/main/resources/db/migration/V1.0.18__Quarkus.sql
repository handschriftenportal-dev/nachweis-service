DELETE from digitalisat d
where d.digitalisate_id is null and not exists(select 1 from katalog k where k.digitalisat_id = d.id);
