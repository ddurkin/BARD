-- Updates for bard_cap_prod and other BARD DBs
-- 11/21/2013

-- un-name the not null constraints for owner_role_id
ALTER TABLE ASSAY DROP CONSTRAINT CK_ASSAY_OWNER_ROLE_ID
;
ALTER TABLE EXPERIMENT DROP CONSTRAINT CK_EXP_OWNER_ROLE_ID
;
ALTER TABLE PANEL DROP CONSTRAINT CK_PANEL_OWNER_ROLE_ID
;
ALTER TABLE PROJECT DROP CONSTRAINT CK_PROJECT_OWNER_ROLE_ID
;

ALTER TABLE ASSAY MODIFY OWNER_ROLE_ID NOT NULL
;
ALTER TABLE EXPERIMENT MODIFY OWNER_ROLE_ID NOT NULL
;
ALTER TABLE PANEL MODIFY OWNER_ROLE_ID NOT NULL
;
ALTER TABLE PROJECT MODIFY OWNER_ROLE_ID NOT NULL
;


-- apply the BARD naming convention to the  fk constraint
ALTER TABLE EXPERIMENT
  RENAME CONSTRAINT FK_EXP_PANEL
  TO FK_EXPRMT_EXPRMT_PANEL
;

-- Add indexes for all FK columns to reduce chances of deadlock
-- Oracle locks the whole table during RI checking if there is no index

CREATE INDEX FK_ASSAY_OWNER_ROLE_ID
    ON ASSAY(OWNER_ROLE_ID)
STORAGE(BUFFER_POOL DEFAULT)
NOPARALLEL
NOCOMPRESS
;

CREATE INDEX FK_ASSAY_CONTEXT_ASSAY
    ON ASSAY_CONTEXT(ASSAY_ID)
STORAGE(BUFFER_POOL DEFAULT)
NOPARALLEL
NOCOMPRESS
;

ALTER TABLE EXT_ONTOLOGY_TREE
ADD CONSTRAINT PK_EXT_ONTOLOGY_TREE
PRIMARY KEY (NODE_ID)
;

-- Add Referencing Foreign Keys SQL

ALTER TABLE EXT_ONTOLOGY_TREE
    ADD CONSTRAINT FK_EXT_ONTOLOGY_TREE_PARENT
FOREIGN KEY (PARENT_NODE_ID)
REFERENCES EXT_ONTOLOGY_TREE (NODE_ID)
ENABLE VALIDATE
;

-- add missing FK constraints
ALTER TABLE PANEL_EXPERIMENT ADD CONSTRAINT FK_PANEL_EXPRMT_PANEL FOREIGN KEY (PANEL_ID)
    REFERENCES PANEL(PANEL_ID)
;

