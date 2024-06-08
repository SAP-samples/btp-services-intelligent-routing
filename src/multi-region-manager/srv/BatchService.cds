using BATCH from '../db/batch-schema';


@path : 'batch'
service BatchService {
    entity BatchJobExecution         as projection on BATCH.JOB_EXECUTION;
    entity BatchJobInstance          as projection on BATCH.JOB_INSTANCE;
    entity BatchJobExecutionContext  as projection on BATCH.JOB_EXECUTION_CONTEXT;
    entity BatchStepExecution        as projection on BATCH.STEP_EXECUTION;
    entity BatchStepExecutionContext as projection on BATCH.STEP_EXECUTION_CONTEXT;
     
    entity BatchJobExecutionEx as  select from BatchJobExecution as je 
        inner join BATCH.JOB_EXECUTION_NOTES as jn on (je.JOB_EXECUTION_ID = jn.JOB_EXECUTION_ID) {
        key je.JOB_EXECUTION_ID,
        je.VERSION,             
        je.JOB_INSTANCE_ID,
        je.CREATE_TIME,
        je.START_TIME,
        je.END_TIME,
        je.STATUS,
        je.EXIT_CODE,
        je.EXIT_MESSAGE,
        je.LAST_UPDATED,
        je.JOB_INSTANCE, 
        jn.JOB_EXECUTION_DESC            
    };
    entity PerformanceAnalytics as 
        select from BatchJobExecution as je 
        inner join BatchJobInstance as ji on (je.JOB_INSTANCE_ID = ji.JOB_INSTANCE_ID) inner join BATCH.JOB_EXECUTION_NOTES as jn on (je.JOB_EXECUTION_ID = jn.JOB_EXECUTION_ID) inner join BatchStepExecution as se on (je.JOB_EXECUTION_ID = se.JOB_EXECUTION_ID) {
            key je.JOB_EXECUTION_ID as JOB_EXECUTION_ID,
            key se.STEP_EXECUTION_ID as STEP_EXECUTION_ID,
            ji.JOB_NAME as JOB_NAME,
            jn.JOB_EXECUTION_TYPE as JOB_EXECUTION_TYPE,
            jn.JOB_EXECUTION_DESC as JOB_EXECUTION_DESC,
            jn.PERFORMANCE_TRACE_REGION as PERFORMANCE_TRACE_REGION,        
            se.STEP_NAME as STEP_NAME,
            NANO100_BETWEEN(se.START_TIME, se.END_TIME) as timeTakenNano:Integer64,
            SECONDS_BETWEEN(se.START_TIME, se.END_TIME) as timeTakenSeconds:Integer64,
            DAYS_BETWEEN(se.START_TIME, se.END_TIME) as daysTaken:Integer64,
     } where jn.PERFORMANCE_TRACE = true;

     @Aggregation.ApplySupported.PropertyRestrictions : true
     @Aggregation.ApplySupported.Transformations     : [
        'aggregate',
        'topcount',
        'bottomcount',
        'identity',
        'concat',
        'groupby',
        'filter',
        'search'
    ]
    entity Performance as select from PerformanceAnalytics as pa {
        key JOB_EXECUTION_ID,
        key STEP_EXECUTION_ID,
        @Analytics.Dimension : true
        @Common.Label: 'Job Name'
        JOB_NAME,
        @Analytics.Dimension : true
        @Common.Label: 'Job Description'
        JOB_EXECUTION_DESC,
        @Analytics.Dimension : true
        @Common.Label: 'Step Name Full'
        STEP_NAME,
        @Analytics.Dimension : true
        @Common.Label: 'Region'
        PERFORMANCE_TRACE_REGION,
        @Analytics.Dimension : true
        @Common.Label: 'Step Name'
        substring(STEP_NAME,1,instr(STEP_NAME,'[')-1) as STEP_NAME_EX:String,
        @Analytics.Measure  : true
        @Aggregation.default : #AVG
        timeTakenSeconds as avgTimeTakenSeconds:Decimal
     } ;
     entity HanaCreatePerformance as select from Performance as perf {
        *       
     } where perf.STEP_NAME = 'Create Subscription [primary]' or perf.STEP_NAME = 'Create Subscription [secondary]';
     
     entity HanaDeletePerformance as select from Performance as perf {
        *
    } where perf.STEP_NAME = 'Delete Subscription [primary]' or perf.STEP_NAME = 'Delete Subscription [secondary]';

    entity OverallPerformance as select from Performance as perf {
        *
    } 
}
    