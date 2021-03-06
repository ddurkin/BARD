
CREATE OR REPLACE PACKAGE auditing_setup
AUTHID CURRENT_USER
AS

    PROCEDURE setup_tables;

    PROCEDURE update_settings( avi_table_name IN VARCHAR2 DEFAULT NULL,
                               avi_Increment VARCHAR2 DEFAULT 'Increment');

END auditing_setup;
/


CREATE OR REPLACE PACKAGE BODY auditing_setup
AS

---------------------------------------------------------------------------------------
-- public facing one
    PROCEDURE setup_tables
    AS
        lv_sql  VARCHAR2(10000);
        ln_table_exists INTEGER := 0;

    BEGIN
      -----------------------------------------------------------
      -- audit_row_log
      SELECT Count(*) INTO ln_table_exists
      FROM tabs
      WHERE table_name = 'AUDIT_ROW_LOG';

      IF ln_table_exists = 0
      THEN
          lv_sql := 'CREATE TABLE audit_row_log
  (audit_id NUMBER (19,0) NOT NULL,
  table_owner VARCHAR2(30) DEFAULT Lower(USER) NOT NULL,
  table_name varchar2(30) NOT NULL,
  primary_key VARCHAR2(4000) NOT NULL,
  action VARCHAR2(20) NOT NULL,
  audit_timestamp DATE DEFAULT SYSDATE  NOT NULL,
  username  VARCHAR2(40) NOT NULL,
  constraint pk_audit_row_log PRIMARY KEY (audit_id)
  )';
          EXECUTE IMMEDIATE lv_sql;
      END IF;

      SELECT Count(*) INTO ln_table_exists
      FROM user_sequences
      WHERE sequence_name = 'AUDIT_ID_SEQ';

      IF ln_table_exists = 0
      THEN
          lv_sql := 'CREATE SEQUENCE audit_id_seq
    START WITH 1
    INCREMENT BY 1
    NOMINVALUE
    MAXVALUE 999999999999999999
    NOCYCLE
    CACHE 200
    NOORDER';
          EXECUTE IMMEDIATE lv_sql;
      END IF;
      SELECT Count(*) INTO ln_table_exists
      FROM user_indexes
      WHERE index_name = 'IDX_AUDIT_ROW_LOG_PRIMARY_KEY';

      IF ln_table_exists = 0
      THEN
          LV_SQL := 'CREATE INDEX idx_audit_row_log_primary_key ON audit_row_log(primary_key)';
          EXECUTE IMMEDIATE lv_sql;
      END IF;
      -----------------------------------------------------------
      -- audit_column_log
      SELECT Count(*) INTO ln_table_exists
      FROM tabs
      WHERE table_name = 'AUDIT_COLUMN_LOG';

      IF ln_table_exists < 1
      THEN
          lv_sql := 'CREATE TABLE audit_column_log
    (audit_id NUMBER (19,0) NOT NULL,
    column_name VARCHAR2(30) NOT NULL,
    old_value VARCHAR2(4000),
    CONSTRAINT pk_audit_column_log PRIMARY KEY (audit_id, column_name),
    CONSTRAINT fk_audit_column_audit_row_log FOREIGN KEY (audit_id)
        REFERENCES audit_row_log (audit_id)
    )';

          EXECUTE IMMEDIATE lv_sql;
          -- Add the foreign key index
          LV_SQL := 'CREATE INDEX fk_audit_column_audit_row ON audit_column_log(audit_id)';
          EXECUTE IMMEDIATE lv_sql;

      END IF;

      -----------------------------------------------------------
      -- audit_setting
      SELECT Count(*) INTO ln_table_exists
      FROM tabs
      WHERE table_name = 'AUDIT_SETTING';

      IF ln_table_exists < 1
      THEN
          lv_sql := 'CREATE TABLE audit_setting
    (table_owner  VARCHAR2(30) NOT NULL,
     table_name VARCHAR2(30) NOT NULL,
     column_name VARCHAR2(30) NOT NULL,
     is_pk  CHAR(1) DEFAULT ''N'' NOT NULL,
     audit_insert CHAR(1) DEFAULT ''N'' NOT NULL,
     audit_update CHAR(1) DEFAULT ''Y'' NOT NULL,
     audit_delete CHAR(1) DEFAULT ''Y'' NOT NULL,
     constraint pk_audit_setting PRIMARY KEY (table_owner, table_name, column_name)
     )';

          EXECUTE IMMEDIATE lv_sql;

          update_settings('', 'Refresh');

      end IF;
    END setup_tables;

    procedure drop_table_if_exists(p_table_name varchar2) is
      ln_table_exists integer;
      l_sql varchar2(1000);
    begin
      select count(1) into ln_table_exists from tabs where table_name = p_table_name;

      if ln_table_exists = 1 then
        l_sql := 'drop table '||p_table_name;
        execute immediate l_sql;
      end if;

    end;


    proCEDURE update_settings(avi_table_name VARCHAR2 DEFAULT NULL,
                              avi_Increment VARCHAR2 DEFAULT 'Increment')
    AS
        lv_sql    VARCHAR2(10000);

    BEGIN
        IF avi_increment = 'Refresh'
        THEN
            lv_sql := 'delete from audit_setting';
        ELSE
            lv_sql := 'delete from audit_setting where table_name = ''' || avi_table_name || '''';
        END IF;
        EXECUTE IMMEDIATE lv_sql;

        drop_table_if_exists('TEMP_COLS');
        EXECUTE IMMEDIATE 'create table temp_cols as select TABLE_NAME,COLUMN_NAME, data_type from cols';
        drop_table_if_exists('TEMP_CONS');
        EXECUTE IMMEDIATE 'create table temp_cons as select owner, table_name, constraint_name, CONSTRAINT_TYPE, INDEX_NAME from user_constraints where constraint_type = ''P''';
        --drop_table_if_exists('temp_ind_columns');
        --EXECUTE IMMEDIATE 'create table temp_ind_columns as select index_name, column_name from user_ind_columns';

        -- Populate with default value
        -- do not track PK, LOB, any columns that are unchanging (date_created)
        -- edit to suit the schema and important tables you want to track
        LV_SQL := 'INSERT INTO AUDIT_SETTING
SELECT user,
    cols.TABLE_NAME,
    cols.COLUMN_NAME,
    Decode(ac.constraint_name, NULL, ''N'', ''Y'') IS_PK,
    ''N'' AUDIT_INSERT,
    Decode(ac.constraint_name, NULL, Decode (DATA_TYPE, ''CLOB'', ''N'', Decode(cols.COLUMN_NAME, ''VERSION'', ''N'', ''Y'')),''N'') AUDIT_UPDATE,
    Decode(ac.constraint_name, NULL, Decode (DATA_TYPE, ''CLOB'', ''N'', ''Y''), ''N'') AUDIT_DELETE
FROM temp_cols cols
left OUTER JOIN (temp_cons ac
                JOIN user_ind_columns aic
                ON aic.index_name = ac.index_name
                   AND AC.CONSTRAINT_TYPE = ''P'')
          ON ac.owner = USER
             AND ac.table_name = cols.table_name
             AND aic.column_name = cols.column_name
WHERE table_name in (select table_name from tabs)
  AND TABLE_NAME NOT LIKE ''AUDIT%''
  AND TABLE_NAME NOT LIKE ''%0%''
  AND TABLE_NAME NOT LIKE ''TEMP%''
  AND TABLE_NAME NOT LIKE ''DATA%''
  AND TABLE_NAME NOT LIKE ''%TREE''
  AND TABLE_NAME NOT LIKE ''MIGRAT%''
  AND TABLE_NAME NOT LIKE ''%LOG''
  AND TABLE_NAME NOT LIKE ''OFFLINE_VALIDATION%''
  AND TABLE_NAME NOT LIKE ''SC_%''
  AND TABLE_NAME NOT LIKE ''MOL_SC%''';
      IF avi_table_name IS NOT NULL
      THEN
          lv_sql := lv_sql || ' and table_name = ''' || Upper(avi_table_name) || '''';
      END IF;
      lv_sql := lv_sql || '  AND NOT EXISTS (SELECT 1
      FROM audit_setting a
      WHERE a.table_owner = USER
        AND a.table_name = cols.table_name
        AND a.column_name = cols.column_name)
ORDER BY table_name, column_name';
          EXECUTE IMMEDIATE lv_sql;

          EXECUTE IMMEDIATE 'Commit';
    END update_settings;

END auditing_setup;
/
