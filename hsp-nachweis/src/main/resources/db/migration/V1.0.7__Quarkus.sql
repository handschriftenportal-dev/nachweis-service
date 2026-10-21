alter table bearbeiter
    add if not exists rolle varchar(255);

alter table bearbeiter
    add if not exists person_id varchar(255)
        constraint fk_normdatenreferenz_bearbeiter_person references normdatenreferenz (id);