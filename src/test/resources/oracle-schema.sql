---
--- Schema generated by BarleyDB static definitions ---
---
---

create table ACL_ACCESS_AREA (
  ID NUMBER(19) NOT NULL,
  NAME VARCHAR(50) NOT NULL,
  MODIFIED_AT TIMESTAMP NOT NULL,
  PARENT_ID NUMBER(19) NULL
);

create table ACL_USER (
  ID NUMBER(19) NOT NULL,
  ACCESS_AREA_ID NUMBER(19) NOT NULL,
  UUID CHAR(60) NOT NULL,
  MODIFIED_AT TIMESTAMP NOT NULL,
  USER_NAME VARCHAR(50) NOT NULL
);

alter table ACL_ACCESS_AREA add constraint PK_ACCESS_AREA primary key (ID);
alter table ACL_USER add constraint PK_USER primary key (ID);

alter table ACL_ACCESS_AREA add constraint FK_ACCESS_AREA_ACCESS_AREA foreign key (PARENT_ID) references ACL_ACCESS_AREA(ID);
alter table ACL_USER add constraint FK_USER_ACCESS_AREA foreign key (ACCESS_AREA_ID) references ACL_ACCESS_AREA(ID);

alter table ACL_USER add constraint UC_USER_1 unique (USER_NAME,ACCESS_AREA_ID);

create table SS_SYNTAX_MODEL (
  ID NUMBER(19) NOT NULL,
  ACCESS_AREA_ID NUMBER(19) NOT NULL,
  UUID CHAR(60) NOT NULL,
  MODIFIED_AT TIMESTAMP NOT NULL,
  NAME VARCHAR(50) NOT NULL,
  STRUCTURE_TYPE NUMBER(9) NOT NULL,
  SYNTAX_TYPE NUMBER(9) NOT NULL,
  USER_ID NUMBER(19) NULL,
  STRUCTURE_ID NUMBER(19) NOT NULL
);

create table SS_XMLSTRUCTURE (
  ID NUMBER(19) NOT NULL,
  ACCESS_AREA_ID NUMBER(19) NOT NULL,
  UUID CHAR(60) NOT NULL,
  MODIFIED_AT TIMESTAMP NOT NULL,
  NAME VARCHAR(50) NOT NULL
);

create table SS_XML_MAPPING (
  ID NUMBER(19) NOT NULL,
  SYNTAX_MODEL_ID NUMBER(19) NOT NULL,
  SUB_SYNTAX_ID NUMBER(19) NULL,
  XPATH VARCHAR(150) NULL,
  TARGET_FIELD_NAME VARCHAR(150) NULL
);

create table SS_CSVSTRUCTURE (
  ID NUMBER(19) NOT NULL,
  ACCESS_AREA_ID NUMBER(19) NOT NULL,
  UUID CHAR(60) NOT NULL,
  MODIFIED_AT TIMESTAMP NOT NULL,
  NAME VARCHAR(50) NOT NULL,
  HEADER_BASED_MAPPING NUMBER(9) NOT NULL
);

create table SS_CSVSTRUCTURE_FIELD (
  ID NUMBER(19) NOT NULL,
  NAME VARCHAR(50) NULL,
  CSVSTRUCTURE_ID NUMBER(19) NOT NULL,
  COLUMN_INDEX NUMBER(9) NOT NULL,
  OPTIONAL NUMBER(9) NOT NULL
);

create table SS_CSV_MAPPING (
  ID NUMBER(19) NOT NULL,
  SYNTAX_MODEL_ID NUMBER(19) NOT NULL,
  CSVSTRUCTURE_FIELD_ID NUMBER(19) NOT NULL,
  TARGET_FIELD_NAME VARCHAR(150) NULL
);

create table SS_TEMPLATE (
  ID NUMBER(19) NOT NULL,
  ACCESS_AREA_ID NUMBER(19) NOT NULL,
  UUID CHAR(60) NOT NULL,
  MODIFIED_AT TIMESTAMP NOT NULL,
  NAME VARCHAR(50) NOT NULL
);

create table SS_TEMPLATE_CONTENT (
  ID NUMBER(19) NOT NULL,
  NAME VARCHAR(50) NOT NULL,
  MODIFIED_AT TIMESTAMP NOT NULL,
  TEMPLATE_ID NUMBER(19) NOT NULL
);

create table SS_DATATYPE (
  ID NUMBER(19) NOT NULL,
  ACCESS_AREA_ID NUMBER(19) NOT NULL,
  UUID CHAR(60) NOT NULL,
  MODIFIED_AT TIMESTAMP NOT NULL,
  NAME VARCHAR(50) NOT NULL
);

create table SS_TEMPLATE_DATATYPE (
  ID NUMBER(19) NOT NULL,
  TEMPLATE_ID NUMBER(19) NOT NULL,
  DATATYPE_ID NUMBER(19) NOT NULL
);

create table SS_RAWDATA (
  ID NUMBER(19) NOT NULL,
  DATA BLOB NOT NULL,
  CHARACTER_ENCODING VARCHAR(50) NULL
);

alter table SS_SYNTAX_MODEL add constraint PK_SYNTAX_MODEL primary key (ID);
alter table SS_XMLSTRUCTURE add constraint PK_XMLSTRUCTURE primary key (ID);
alter table SS_XML_MAPPING add constraint PK_XML_MAPPING primary key (ID);
alter table SS_CSVSTRUCTURE add constraint PK_CSVSTRUCTURE primary key (ID);
alter table SS_CSVSTRUCTURE_FIELD add constraint PK_CSVSTRUCTURE_FIELD primary key (ID);
alter table SS_CSV_MAPPING add constraint PK_CSV_MAPPING primary key (ID);
alter table SS_TEMPLATE add constraint PK_TEMPLATE primary key (ID);
alter table SS_TEMPLATE_CONTENT add constraint PK_TEMPLATE_CONTENT primary key (ID);
alter table SS_DATATYPE add constraint PK_DATATYPE primary key (ID);
alter table SS_TEMPLATE_DATATYPE add constraint PK_TEMPLATE_DATATYPE primary key (ID);
alter table SS_RAWDATA add constraint PK_RAWDATA primary key (ID);

alter table SS_SYNTAX_MODEL add constraint FK_SYNTAX_MODEL_ACCESS_AREA foreign key (ACCESS_AREA_ID) references ACL_ACCESS_AREA(ID);
alter table SS_SYNTAX_MODEL add constraint FK_SYNTAX_MODEL_USER foreign key (USER_ID) references ACL_USER(ID);
alter table SS_XMLSTRUCTURE add constraint FK_XMLSTRUCTURE_ACCESS_AREA foreign key (ACCESS_AREA_ID) references ACL_ACCESS_AREA(ID);
alter table SS_XML_MAPPING add constraint FK_XML_MAPPING_SYNTAX_MODEL foreign key (SYNTAX_MODEL_ID) references SS_SYNTAX_MODEL(ID);
alter table SS_XML_MAPPING add constraint FK_XML_MAPPING_SUBSYNTAX_MODEL foreign key (SUB_SYNTAX_ID) references SS_SYNTAX_MODEL(ID);
alter table SS_CSVSTRUCTURE add constraint FK_CSVSTRUCTURE_ACCESS_AREA foreign key (ACCESS_AREA_ID) references ACL_ACCESS_AREA(ID);
alter table SS_CSVSTRUCTURE_FIELD add constraint FK_CSVSTRUCTURE_FIELD_CSVSTRUCTURE foreign key (CSVSTRUCTURE_ID) references SS_CSVSTRUCTURE(ID);
alter table SS_CSV_MAPPING add constraint FK_CSV_MAPPING_SYNTAX_MODEL foreign key (SYNTAX_MODEL_ID) references SS_SYNTAX_MODEL(ID);
alter table SS_CSV_MAPPING add constraint FK_CSV_MAPPING_CSVSTRUCTURE_FIELD foreign key (CSVSTRUCTURE_FIELD_ID) references SS_CSVSTRUCTURE_FIELD(ID);
alter table SS_TEMPLATE add constraint FK_TEMPLATE_ACCESS_AREA foreign key (ACCESS_AREA_ID) references ACL_ACCESS_AREA(ID);
alter table SS_TEMPLATE_CONTENT add constraint FK_TEMPLATE_CONTENT_TEMPLATE foreign key (TEMPLATE_ID) references SS_TEMPLATE(ID);
alter table SS_DATATYPE add constraint FK_DATATYPE_ACCESS_AREA foreign key (ACCESS_AREA_ID) references ACL_ACCESS_AREA(ID);
alter table SS_TEMPLATE_DATATYPE add constraint FK_TEMPLATE_DATATYPE_TEMPLATE foreign key (TEMPLATE_ID) references SS_TEMPLATE(ID);
alter table SS_TEMPLATE_DATATYPE add constraint FK_TEMPLATE_DATATYPE_DATATYPE foreign key (DATATYPE_ID) references SS_DATATYPE(ID);

alter table SS_SYNTAX_MODEL add constraint UC_SYNTAX_MODEL_1 unique (NAME,ACCESS_AREA_ID);
alter table SS_XMLSTRUCTURE add constraint UC_XMLSTRUCTURE_1 unique (NAME,ACCESS_AREA_ID);
alter table SS_CSVSTRUCTURE add constraint UC_CSVSTRUCTURE_1 unique (NAME,ACCESS_AREA_ID);
alter table SS_TEMPLATE add constraint UC_TEMPLATE_1 unique (NAME,ACCESS_AREA_ID);
alter table SS_DATATYPE add constraint UC_DATATYPE_1 unique (NAME,ACCESS_AREA_ID);