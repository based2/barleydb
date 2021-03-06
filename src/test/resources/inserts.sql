/*
 #%L
  BarleyDB
   %%
   Copyright (C) 2014 Scott Sinclair <scottysinclair@gmail.com>
   %%
   All rights reserved.
   #L%
*/

insert into ACL_ACCESS_AREA (ID, NAME, MODIFIED_AT) values (1, 'Root', '2014-01-01 00:00:00.00000');

insert into ACL_USER (ID, USER_NAME, MODIFIED_AT, ACCESS_AREA_ID, UUID) values (1, 'Scott', '2014-01-01 00:00:00.00000', 1, 'UUID');
insert into ACL_USER (ID, USER_NAME, MODIFIED_AT, ACCESS_AREA_ID, UUID) values (2, 'Katerina', '2014-01-01 00:00:00.00000', 1, 'UUID');

insert into SS_XMLSTRUCTURE(ID, NAME, ACCESS_AREA_ID, MODIFIED_AT, UUID) values (1, 'SWIFT-1', 1, '2014-01-01 00:00:00.00000', 'UUID');
insert into SS_XMLSTRUCTURE(ID, NAME, ACCESS_AREA_ID, MODIFIED_AT, UUID) values (2, 'SWIFT-2', 1, '2014-01-01 00:00:00.00000', 'UUID');

insert into SS_SYNTAX_MODEL (ID, NAME, ACCESS_AREA_ID, SYNTAX_TYPE, STRUCTURE_TYPE, USER_ID, STRUCTURE_ID, MODIFIED_AT, UUID) values (1, 'syntax-xml-1', 1, 0, 1, 1, 1, '2014-01-01 00:00:00.00000', 'UUID');
insert into SS_SYNTAX_MODEL (ID, NAME, ACCESS_AREA_ID, SYNTAX_TYPE, STRUCTURE_TYPE, USER_ID, STRUCTURE_ID, MODIFIED_AT, UUID) values (2, 'syntax-xml-2', 1, 1, 1, 2, 2, '2014-01-01 00:00:00.00000', 'UUID');

insert into SS_XML_MAPPING (ID, TARGET_FIELD_NAME, XPATH, SYNTAX_MODEL_ID, SUB_SYNTAX_ID) values (1, 'tfn11', 'sfn11', 1, null);
insert into SS_XML_MAPPING (ID, TARGET_FIELD_NAME, XPATH, SYNTAX_MODEL_ID, SUB_SYNTAX_ID) values (2, 'tfn12', 'sfn12', 1, null);
insert into SS_XML_MAPPING (ID, TARGET_FIELD_NAME, XPATH, SYNTAX_MODEL_ID,SUB_SYNTAX_ID) values (3, 'tfn13', 'sfn13', 1, 2);

insert into SS_XML_MAPPING (ID, TARGET_FIELD_NAME, XPATH, SYNTAX_MODEL_ID, SUB_SYNTAX_ID) values (4, 'tfn21', 'sfn21', 2, null);
insert into SS_XML_MAPPING (ID, TARGET_FIELD_NAME, XPATH, SYNTAX_MODEL_ID, SUB_SYNTAX_ID) values (5, 'tfn22', 'sfn22', 2, null);
insert into SS_XML_MAPPING (ID, TARGET_FIELD_NAME, XPATH, SYNTAX_MODEL_ID, SUB_SYNTAX_ID) values (6, 'tfn23', 'sfn23', 2, null);

insert into SS_TEMPLATE (ID, NAME, ACCESS_AREA_ID, MODIFIED_AT, UUID) values (1, 'template-1', 1, '2014-01-01 00:00:00.00000', 'UUID');
insert into SS_TEMPLATE (ID, NAME, ACCESS_AREA_ID, MODIFIED_AT, UUID) values (2, 'template-2', 1, '2014-01-01 00:00:00.00000', 'UUID');

insert into SS_TEMPLATE_CONTENT (ID, TEMPLATE_ID, NAME, MODIFIED_AT) values (1, 1, 'template-1-c1', '2014-01-01 00:00:00.00000');
insert into SS_TEMPLATE_CONTENT (ID, TEMPLATE_ID, NAME, MODIFIED_AT) values (2, 1, 'template-1-c2', '2014-01-01 00:00:00.00000');
insert into SS_TEMPLATE_CONTENT (ID, TEMPLATE_ID, NAME, MODIFIED_AT) values (3, 2, 'template-2-c1', '2014-01-01 00:00:00.00000');

insert into SS_DATATYPE (ID, NAME, ACCESS_AREA_ID, MODIFIED_AT, UUID) values (1, 'datatype-1', 1, '2014-01-01 00:00:00.00000', 'UUID');
insert into SS_DATATYPE (ID, NAME, ACCESS_AREA_ID, MODIFIED_AT, UUID) values (2, 'datatype-1.1', 1, '2014-01-01 00:00:00.00000', 'UUID');
insert into SS_DATATYPE (ID, NAME, ACCESS_AREA_ID, MODIFIED_AT, UUID) values (3, 'datatype-2', 1, '2014-01-01 00:00:00.00000', 'UUID');

insert into SS_TEMPLATE_DATATYPE (ID, TEMPLATE_ID, DATATYPE_ID) values (1, 1, 1);
insert into SS_TEMPLATE_DATATYPE (ID, TEMPLATE_ID, DATATYPE_ID) values (2, 1, 2);
insert into SS_TEMPLATE_DATATYPE (ID, TEMPLATE_ID, DATATYPE_ID) values (3, 2, 1);
insert into SS_TEMPLATE_DATATYPE (ID, TEMPLATE_ID, DATATYPE_ID) values (4, 2, 3);


insert into SS_CSVSTRUCTURE(ID, NAME, HEADER_BASED_MAPPING, ACCESS_AREA_ID, MODIFIED_AT, UUID) values (3, 'csv-str-1', 0, 1, '2014-01-01 00:00:00.00000', 'UUID');
insert into SS_CSVSTRUCTURE(ID, NAME, HEADER_BASED_MAPPING, ACCESS_AREA_ID, MODIFIED_AT, UUID) values (4, 'csv-str-2', 0, 1, '2014-01-01 00:00:00.00000', 'UUID');

insert into SS_CSVSTRUCTURE_FIELD(ID, NAME, COLUMN_INDEX, OPTIONAL, CSVSTRUCTURE_ID) values (1, 'field one', 1, 0, 3);
insert into SS_CSVSTRUCTURE_FIELD(ID, NAME, COLUMN_INDEX, OPTIONAL, CSVSTRUCTURE_ID) values (2, 'field two', 2, 0, 3);

insert into SS_CSVSTRUCTURE_FIELD(ID, NAME, COLUMN_INDEX, OPTIONAL, CSVSTRUCTURE_ID) values (3, 'f-one', 1, 1, 4);
insert into SS_CSVSTRUCTURE_FIELD(ID, NAME, COLUMN_INDEX, OPTIONAL, CSVSTRUCTURE_ID) values (4, 'f-two', 2, 1, 4);

insert into SS_SYNTAX_MODEL (ID, NAME, ACCESS_AREA_ID, SYNTAX_TYPE, STRUCTURE_TYPE, USER_ID, STRUCTURE_ID, MODIFIED_AT, UUID) values (3, 'syntax-csv-1', 1, 0, 0, 2, 3, '2014-01-01 00:00:00.00000', 'UUID');
insert into SS_SYNTAX_MODEL (ID, NAME, ACCESS_AREA_ID, SYNTAX_TYPE, STRUCTURE_TYPE, USER_ID, STRUCTURE_ID, MODIFIED_AT, UUID) values (4, 'syntax-csv-2', 1, 0, 0, 2, 4, '2014-01-01 00:00:00.00000', 'UUID');

insert into SS_CSV_MAPPING (ID, TARGET_FIELD_NAME, CSVSTRUCTURE_FIELD_ID, SYNTAX_MODEL_ID) values (1, 'tfn11', 1, 3);
insert into SS_CSV_MAPPING (ID, TARGET_FIELD_NAME, CSVSTRUCTURE_FIELD_ID, SYNTAX_MODEL_ID) values (2, 'tfn12', 1, 3);
insert into SS_CSV_MAPPING (ID, TARGET_FIELD_NAME, CSVSTRUCTURE_FIELD_ID, SYNTAX_MODEL_ID) values (3, 'tfn13', 2, 3);

insert into SS_CSV_MAPPING (ID, TARGET_FIELD_NAME, CSVSTRUCTURE_FIELD_ID, SYNTAX_MODEL_ID) values (4, 'tfn21', 3, 4);
insert into SS_CSV_MAPPING (ID, TARGET_FIELD_NAME, CSVSTRUCTURE_FIELD_ID, SYNTAX_MODEL_ID) values (5, 'tfn22', 4, 4);
insert into SS_CSV_MAPPING (ID, TARGET_FIELD_NAME, CSVSTRUCTURE_FIELD_ID, SYNTAX_MODEL_ID) values (6, 'tfn23', 4, 4);
