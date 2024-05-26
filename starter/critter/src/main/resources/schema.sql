create schema if not exists critterdb;

drop table if exists schedule_activity;
drop table if exists schedule_employee;
drop table if exists schedule_pet;
drop table if exists schedule;
drop table if exists customer_pet;
drop table if exists customer;
drop table if exists employee_days_available;
drop table if exists employee_skill;
drop table if exists employee;
drop table if exists pet;
drop table if exists skill;

create table if not exists pet (
    id bigint not null auto_increment,
    type varchar(20) not null,
    name varchar(255) not null,
    owner_id bigint not null,
    birthdate datetime null,
    notes varchar(255) null,
    primary key (id)
);

create table if not exists customer (
    id bigint not null auto_increment,
    name varchar(255) not null,
    phone_number varchar(20) not null,
    notes varchar(255) null,
    primary key (id)
);

create table if not exists customer_pet (
    customer_id bigint not null,
    pet_id bigint not null,
    primary key (customer_id, pet_id),
    foreign key (customer_id) references customer(id) on delete cascade
);

create table if not exists employee (
    id bigint not null auto_increment,
    name varchar(255) not null,
    primary key (id)
);

create table if not exists skill (
    skill_id bigint not null,
    skill_name varchar(50) not null
);

create table if not exists employee_skill (
    skill_id bigint not null,
    employee_id bigint not null,
    primary key (employee_id, skill_id),
    foreign key (employee_id) references employee(id) on delete cascade
);

create table if not exists employee_days_available (
    employee_id bigint not null,
    day_of_week varchar(20) not null,
    primary key (employee_id, day_of_week),
    foreign key (employee_id) references employee(id) on delete cascade
);

create table if not exists schedule (
    id bigint not null auto_increment,
    date varchar(50) not null,
    primary key (id)
);

create table if not exists schedule_employee (
    schedule_id bigint not null,
    employee_id bigint not null,
    primary key (schedule_id, employee_id),
    foreign key (schedule_id) references schedule(id) on delete cascade
);

create table if not exists schedule_pet (
    schedule_id bigint not null,
    pet_id bigint not null,
    primary key (schedule_id, pet_id),
    foreign key (schedule_id) references schedule(id) on delete cascade
);

create table if not exists schedule_activity (
    schedule_id bigint not null,
    skill_id bigint not null,
    primary key (schedule_id, skill_id),
    foreign key (schedule_id) references schedule(id) on delete cascade
);