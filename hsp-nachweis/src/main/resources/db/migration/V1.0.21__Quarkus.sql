create table if not exists digitalisat_normdatenreferenz
(
    digitalisat_id          varchar(255) not null
        constraint fkhje634tusggvxli2559llxc4l
            references digitalisat,
    digitalisatskonzepte_id varchar(255) not null
        constraint fkgh581vwj3vfqig5onvhm8us3k
            references normdatenreferenz,
    constraint digitalisat_digitalisatskonzepte_pkey
        primary key (digitalisat_id, digitalisatskonzepte_id)
);

ALTER TABLE normdatenreferenz
    ADD uri varchar(4096);