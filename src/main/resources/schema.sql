--
-- postgresql
--
--drop sequence if exists HIBERNATE_SEQUENCES;
--create sequence HIBERNATE_SEQUENCES start with 1024 increment by 1;

-- 일정
--drop table vcalendar;
create table vcalendar (
id			serial not null,
title		varchar(1024) not null DEFAULT '',
description	varchar(1048576) not null DEFAULT '',

component_id	integer not null DEFAULT 0,

created		timestamp not null DEFAULT CURRENT_TIMESTAMP,
updated		timestamp not null DEFAULT CURRENT_TIMESTAMP,
primary key (id));
alter SEQUENCE vcalendar_id_seq restart with 1024;

--drop table vcalendar_component;
create table vcalendar_component (
id			serial not null,
content		varchar(1048576) not null DEFAULT '',

vcalendar_id	integer not null DEFAULT 0,
field_start		timestamp not null DEFAULT CURRENT_TIMESTAMP,
field_end		timestamp not null DEFAULT CURRENT_TIMESTAMP,

created		timestamp not null DEFAULT CURRENT_TIMESTAMP,
updated		timestamp not null DEFAULT CURRENT_TIMESTAMP,
primary key (id));
alter SEQUENCE vcalendar_component_id_seq restart with 1024;
