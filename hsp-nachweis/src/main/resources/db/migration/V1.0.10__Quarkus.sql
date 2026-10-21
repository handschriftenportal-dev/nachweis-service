alter table publikation
    drop column if exists jahrderveroeffentlichung;

alter table publikation
    add if not exists datum_der_veroeffentlichung timestamp not null;

alter table publikation
    add if not exists publikations_typ varchar(32) not null;
