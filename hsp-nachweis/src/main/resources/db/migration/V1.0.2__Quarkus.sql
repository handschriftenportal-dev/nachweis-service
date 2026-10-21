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
