/*
 Clean script generated by BarleyDB static definitions
*/
alter table SS_SYNTAX_MODEL drop index UC_SYNTAX_MODEL_1;
alter table SS_SYNTAX_MODEL drop foreign key FK_SYNTAX_MODEL_ACCESS_AREA;
alter table SS_SYNTAX_MODEL drop foreign key FK_SYNTAX_MODEL_USER;
alter table SS_SYNTAX_MODEL drop primary key  PK_SYNTAX_MODEL;
alter table SS_XMLSTRUCTURE drop index UC_XMLSTRUCTURE_1;
alter table SS_XMLSTRUCTURE drop foreign key FK_XMLSTRUCTURE_ACCESS_AREA;
alter table SS_XMLSTRUCTURE drop primary key  PK_XMLSTRUCTURE;
alter table SS_XML_MAPPING drop foreign key FK_XML_MAPPING_SYNTAX_MODEL;
alter table SS_XML_MAPPING drop foreign key FK_XML_MAPPING_SUBSYNTAX_MODEL;
alter table SS_XML_MAPPING drop primary key  PK_XML_MAPPING;
alter table SS_CSVSTRUCTURE drop index UC_CSVSTRUCTURE_1;
alter table SS_CSVSTRUCTURE drop foreign key FK_CSVSTRUCTURE_ACCESS_AREA;
alter table SS_CSVSTRUCTURE drop primary key  PK_CSVSTRUCTURE;
alter table SS_CSVSTRUCTURE_FIELD drop foreign key FK_CSVSTRUCTURE_FIELD_CSVSTRUCTURE;
alter table SS_CSVSTRUCTURE_FIELD drop primary key  PK_CSVSTRUCTURE_FIELD;
alter table SS_CSV_MAPPING drop foreign key FK_CSV_MAPPING_SYNTAX_MODEL;
alter table SS_CSV_MAPPING drop foreign key FK_CSV_MAPPING_CSVSTRUCTURE_FIELD;
alter table SS_CSV_MAPPING drop primary key  PK_CSV_MAPPING;
alter table SS_TEMPLATE drop index UC_TEMPLATE_1;
alter table SS_TEMPLATE drop foreign key FK_TEMPLATE_ACCESS_AREA;
alter table SS_TEMPLATE drop primary key  PK_TEMPLATE;
alter table SS_TEMPLATE_CONTENT drop foreign key FK_TEMPLATE_CONTENT_TEMPLATE;
alter table SS_TEMPLATE_CONTENT drop primary key  PK_TEMPLATE_CONTENT;
alter table SS_DATATYPE drop index UC_DATATYPE_1;
alter table SS_DATATYPE drop foreign key FK_DATATYPE_ACCESS_AREA;
alter table SS_DATATYPE drop primary key  PK_DATATYPE;
alter table SS_TEMPLATE_DATATYPE drop foreign key FK_TEMPLATE_DATATYPE_TEMPLATE;
alter table SS_TEMPLATE_DATATYPE drop foreign key FK_TEMPLATE_DATATYPE_DATATYPE;
alter table SS_TEMPLATE_DATATYPE drop primary key  PK_TEMPLATE_DATATYPE;
alter table SS_RAWDATA drop primary key  PK_RAWDATA;
drop table SS_RAWDATA;
drop table SS_TEMPLATE_DATATYPE;
drop table SS_DATATYPE;
drop table SS_TEMPLATE_CONTENT;
drop table SS_TEMPLATE;
drop table SS_CSV_MAPPING;
drop table SS_CSVSTRUCTURE_FIELD;
drop table SS_CSVSTRUCTURE;
drop table SS_XML_MAPPING;
drop table SS_XMLSTRUCTURE;
drop table SS_SYNTAX_MODEL;

alter table ACL_ACCESS_AREA drop foreign key FK_ACCESS_AREA_ACCESS_AREA;
alter table ACL_ACCESS_AREA drop primary key  PK_ACCESS_AREA;
alter table ACL_USER drop index UC_USER_1;
alter table ACL_USER drop foreign key FK_USER_ACCESS_AREA;
alter table ACL_USER drop primary key  PK_USER;
drop table ACL_USER;
drop table ACL_ACCESS_AREA;