delete from ASSAY_CONTEXT_MEASURE ;
delete from PROJECT_DOCUMENT ;
delete from EXPRMT_CONTEXT_ITEM ;
delete from EXPRMT_CONTEXT ;
delete from PRJCT_EXPRMT_CNTXT_ITEM ;
delete from PRJCT_EXPRMT_CONTEXT ;
delete from STEP_CONTEXT_ITEM ;
delete from STEP_CONTEXT ;
delete from PROJECT_CONTEXT_ITEM ;
delete from PROJECT_CONTEXT ;
delete from ASSAY_CONTEXT_ITEM ;
delete from EXPRMT_MEASURE ;
delete from ASSAY_CONTEXT_MEASURE ;
delete from ASSAY_CONTEXT ;
--delete from DATABASECHANGELOGLOCK ;
--delete from DATABASECHANGELOG ;
delete from UNIT_CONVERSION ;
delete from ELEMENT_HIERARCHY ;
delete from TREE_ROOT ;
delete from ONTOLOGY_ITEM ;
delete from ONTOLOGY ;
delete from LABORATORY_TREE ;
delete from ASSAY_DOCUMENT ;
delete from EXTERNAL_REFERENCE ;
delete from EXTERNAL_SYSTEM ;
delete from STAGE_TREE ;
delete from ASSAY_DESCRIPTOR_TREE ;
delete from UNIT_TREE ;
delete from MEASURE ;
delete from ROLE ;
delete from PROJECT_STEP ;
delete from PROJECT_EXPERIMENT ;
delete from BIOLOGY_DESCRIPTOR_TREE ;
delete from INSTANCE_DESCRIPTOR_TREE ;
delete from RESULT_TYPE_TREE ;
delete from PERSON_ROLE ;
delete from PERSON ;
delete from FAVORITE ;
delete from TEAM_MEMBER ;
delete from TEAM ;
COMMIT;
delete from RSLT_CONTEXT_ITEM ;
delete from RESULT_HIERARCHY ;
COMMIT;
delete from RESULT ;
COMMIT;
--DROP table RUN_CONTEX_10262012225939000 ;
delete from SUBSTANCE ;
--delete from STATEMENT_LOG ;
--DROP table RESULT_HIE_10262012225920000 ;
--DROP table RESULT_10262012225904000 ;
--delete from IDENTIFIER_MAPPING ;
--delete from ERROR_LOG ;
delete from ELEMENT ;
delete from EXPERIMENT ;
delete from ASSAY ;
delete from PROJECT ;

SELECT Count(*) FROM result;