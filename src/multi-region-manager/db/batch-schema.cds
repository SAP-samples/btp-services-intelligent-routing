using {
    managed,
    cuid
} from '@sap/cds/common';

namespace BATCH;

@Catalog.tableType : #COLUMN
entity JOB_INSTANCE {
    key JOB_INSTANCE_ID : Integer64 not null;
        VERSION         : Integer64;
        JOB_NAME        : String(100) not null;
        JOB_KEY         : String(32) not null;
}

@Catalog.tableType : #COLUMN
entity JOB_EXECUTION {
    key JOB_EXECUTION_ID           : Integer64 not null;
        VERSION                    : Integer64;
        JOB_INSTANCE_ID            : Integer64 not null;
        CREATE_TIME                : Timestamp not null;
        START_TIME                 : Timestamp null;
        END_TIME                   : Timestamp;
        STATUS                     : String(10);
        EXIT_CODE                  : String(2500);
        EXIT_MESSAGE               : String(2500);
        LAST_UPDATED               : Timestamp;
        JOB_INSTANCE               : Association to JOB_INSTANCE
                                         on JOB_INSTANCE.JOB_INSTANCE_ID = JOB_INSTANCE_ID;
}

@Catalog.tableType : #COLUMN
@nokey
entity JOB_EXECUTION_PARAMS {
    JOB_EXECUTION_ID : Integer64 not null;
    PARAMETER_NAME       : String(100) not null;
    PARAMETER_TYPE       : String(100) not null;
    PARAMETER_VALUE      : String(2500) ;
    IDENTIFYING      : String(1) not null;
    JOB_EXECUTION    : Association to JOB_EXECUTION
                           on JOB_EXECUTION.JOB_EXECUTION_ID = JOB_EXECUTION_ID;
}

@Catalog.tableType : #COLUMN
entity STEP_EXECUTION {
    key STEP_EXECUTION_ID  : Integer64 not null;
        VERSION            : Integer64 not null;
        STEP_NAME          : String(100) not null;
        JOB_EXECUTION_ID   : Integer64 not null;
        CREATE_TIME        : Timestamp not null; 
        START_TIME         : Timestamp null;
        END_TIME           : Timestamp null;
        STATUS             : String(10);
        COMMIT_COUNT       : Integer64;
        READ_COUNT         : Integer64;
        FILTER_COUNT       : Integer64;
        WRITE_COUNT        : Integer64;
        READ_SKIP_COUNT    : Integer64;
        WRITE_SKIP_COUNT   : Integer64;
        PROCESS_SKIP_COUNT : Integer64;
        ROLLBACK_COUNT     : Integer64;
        EXIT_CODE          : String(2500);
        EXIT_MESSAGE       : String(2500);
        LAST_UPDATED       : Timestamp;
        JOB_EXECUTION      : Association to JOB_EXECUTION
                                 on JOB_EXECUTION.JOB_EXECUTION_ID = JOB_EXECUTION_ID;
}

@Catalog.tableType : #COLUMN
entity STEP_EXECUTION_CONTEXT {
    key STEP_EXECUTION_ID  : Integer64 not null;
        SHORT_CONTEXT      : String(2500) not null;
        SERIALIZED_CONTEXT : LargeString;
        STEP_EXECUTION     : Association to STEP_EXECUTION
                                 on STEP_EXECUTION.STEP_EXECUTION_ID = STEP_EXECUTION_ID;
}

@Catalog.tableType : #COLUMN
entity JOB_EXECUTION_CONTEXT {
    key JOB_EXECUTION_ID   : Integer64 not null;
        SHORT_CONTEXT      : String(2500) not null;
        SERIALIZED_CONTEXT : LargeString;
        JOB_EXECUTION      : Association to JOB_EXECUTION
                                 on JOB_EXECUTION.JOB_EXECUTION_ID = JOB_EXECUTION_ID;
}

@Catalog.tableType : #COLUMN
@nokey
entity JOB_EXECUTION_NOTES {
    key JOB_EXECUTION_ID : Integer64 not null;
        JOB_EXECUTION_TYPE       : String(50) not null;
        PERFORMANCE_TRACE       : Boolean default  false;
        PERFORMANCE_TRACE_REGION  : String(100) null;
        JOB_EXECUTION_DESC       : String(100) null;
        JOB_EXECUTION    : Association to JOB_EXECUTION
                           on JOB_EXECUTION.JOB_EXECUTION_ID = JOB_EXECUTION_ID;
}