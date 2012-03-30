
create table SCHEMA_SET (
	SCHEMA_SET_ID int(10) unsigned not null auto_increment,
	IDENTIFIER varchar(255) not null,
	CONTINUITY_ID varchar(36) not null,
	REG_STATUS enum('Draft','Released') not null default 'Draft',
	WORKING_COPY bool not null default false,
	WORKING_USER varchar(50) default null,
	DATE timestamp not null default now(),
	USER varchar(50) default null,
	COMMENT text default null,
	CHECKEDOUT_COPY_ID int(10) unsigned default null,
	primary key (SCHEMA_SET_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

alter table SCHEMA_SET add index (IDENTIFIER);
alter table SCHEMA_SET add index (CONTINUITY_ID);

create table `SCHEMA` (
	SCHEMA_ID int(10) unsigned not null auto_increment,
	FILENAME varchar(255) not null,
	SCHEMA_SET_ID int(10) unsigned default null,
	CONTINUITY_ID varchar(36) not null,
	REG_STATUS enum('Draft','Released') not null default 'Draft',
	WORKING_COPY bool not null default false,
	WORKING_USER varchar(50) default null,
	DATE timestamp not null default now(),
	USER varchar(50) default null,
	COMMENT text default null,
	CHECKEDOUT_COPY_ID int(10) unsigned default null,
	primary key (SCHEMA_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

alter table `SCHEMA` add index (FILENAME);
alter table `SCHEMA` add index (SCHEMA_SET_ID);
alter table `SCHEMA` add index (CONTINUITY_ID);

delete from ATTRIBUTE where PARENT_TYPE='C' or PARENT_TYPE='CSI';
alter table ATTRIBUTE change column PARENT_TYPE PARENT_TYPE enum('E', 'T', 'DS', 'SC', 'SCS') not null default 'E';