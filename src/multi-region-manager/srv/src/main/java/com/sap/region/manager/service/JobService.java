package com.sap.region.manager.service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.support.ReferenceJobFactory;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;

import com.sap.region.manager.steps.aem.ReplicationRoleChange;
import com.sap.region.manager.steps.azure.TMPriorityChange;
import com.sap.region.manager.steps.azure.TMRoutingChange;
import com.sap.region.manager.steps.db.CreateRemoteSubscription;
import com.sap.region.manager.steps.db.DeleteRemoteSubscription;
import com.sap.region.manager.steps.db.ReadRemoteSubscription;
import com.sap.region.manager.steps.db.ReadRemoteTable;
import com.sap.region.manager.steps.integration.DeployArtifacts;
import com.sap.region.manager.steps.integration.UndeployArtifacts;
import com.sap.region.manager.steps.monitor.ReplicationMonitor;

/**
 * <p>
 * JobService class.
 * </p>
 *
 * @author shanthakumar.krishnaswamy@sap.com
 */
@Service
public class JobService {
    @Autowired
    private JobRepository jobRepository;

    @Autowired
    JobRegistry jobRegistry;

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private JobOperator jobOperator;

    @Autowired
    private JobExplorer jobExplorer;
    
    @Autowired
    private AsyncTaskExecutor asyncTaskExecutor;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    @Qualifier("jdbcTemplateMap")
    private Map<String, JdbcTemplate> jdbcTemplateMap;

    @Autowired
    @Qualifier("jdbcTemplate")
    private JdbcTemplate jdbcTemplate;

    @Autowired
    @Qualifier("ciPropertiesMap")
    private Map<String, Properties> ciPropertiesMap;
    
    @Value("${spring.cloudintegration.package_ids}")
    private String[] ciPackageIds;

    @Autowired
    @Qualifier("aemPropertiesMap")
    private Map<String, Properties> aemPropertiesMap;

    @Autowired
    @Qualifier("azureProperties")
    private Properties azureProperties;

    @Autowired
    @Qualifier("regionProperties")
    private Properties regionProperties;

    @Value("${spring.remotesource}")
    private String remoteSource;
    
    @Value("${PERFORMANCE_TRACE:false}")
    private boolean performanceTrace;
    
    @Value("${PERFORMANCE_TRACE_REGION:}")
    private String performanceTraceRegion;
    private JobExecutionListener listener = new JobExecutionListener() {
        @Override
        public void afterJob(JobExecution jobExecution) {
            if (Objects.equals(jobExecution.getStatus(), BatchStatus.COMPLETED)) {
                jobExecution.setExitStatus(new ExitStatus(jobExecution.getExitStatus().getExitCode(), jobExecution.getJobInstance().getJobName()+" completed successfully"));
                
            }
        }
        
    };

    public Long fired(String region, Long routingPriority) throws Exception {
        String alternateRegion = region.equals("primary") ? "secondary" : "primary";
        Step tmRoutingPriorityChangeStep = new StepBuilder("Routing Priority Change [" + routingPriority + "]",
                jobRepository).allowStartIfComplete(true)
                .tasklet(new TMPriorityChange(azureProperties, routingPriority), transactionManager).build();
        Step replicationMonitorStep = new StepBuilder("Monitor Replication [" + regionProperties.getProperty(alternateRegion) + "]",jobRepository).allowStartIfComplete(true)
                               .tasklet(new ReplicationMonitor(aemPropertiesMap.get(alternateRegion), jdbcTemplateMap.get(alternateRegion), alternateRegion), transactionManager).build();
        Step deleteSubscriptionStep = new StepBuilder("Delete Subscription [" + regionProperties.getProperty(alternateRegion) + "]", jobRepository)
                .allowStartIfComplete(true).<Object, List<String>>chunk(1, transactionManager)
                .reader(new ReadRemoteSubscription(jdbcTemplateMap.get(alternateRegion), remoteSource))
                .writer(new DeleteRemoteSubscription(jdbcTemplateMap.get(alternateRegion), remoteSource,
                        alternateRegion))
                .build();
        Properties properties = ciPropertiesMap.get(alternateRegion);
        Step deployArtifactStep = new StepBuilder("Deploy Artifacts [" + regionProperties.getProperty(alternateRegion) + "]", jobRepository)
                .allowStartIfComplete(true)
                .tasklet(new DeployArtifacts(properties, ciPackageIds, alternateRegion), transactionManager).build();

        String replicationRole = "standby";
        properties = aemPropertiesMap.get(region);
        Step aemReplicationStandby = new StepBuilder(
                "AEM Replication Role Change [" + regionProperties.getProperty(region) + "] [" + replicationRole + "]", jobRepository)
                .allowStartIfComplete(true)
                .tasklet(new ReplicationRoleChange(properties, replicationRole, region), transactionManager).build();
        properties = aemPropertiesMap.get(alternateRegion);
        replicationRole = "active";
        Step aemReplicationActive = new StepBuilder(
                "AEM Replication Role Change [" + regionProperties.getProperty(alternateRegion) + "] [" + replicationRole + "]", jobRepository)
                .allowStartIfComplete(true)
                .tasklet(new ReplicationRoleChange(properties, replicationRole, alternateRegion), transactionManager)
                .build();
        Job job = new JobBuilder("Region Failed [" + regionProperties.getProperty(region) + "]", jobRepository).incrementer(new RunIdIncrementer())
                .start(tmRoutingPriorityChangeStep).next(replicationMonitorStep).next(deleteSubscriptionStep).next(aemReplicationStandby)
                .next(aemReplicationActive).next(deployArtifactStep).build();
        JobExecution execution = jobLauncher.run(job, new JobParameters());
        Long executionId = execution.getId();
        insertNotes(executionId, "fired", "Region Failed");
        return executionId;
    }

    public Long resolved(String region) throws Exception {
        Step createSubscriptionStep = new StepBuilder("Create Subscription [" + regionProperties.getProperty(region) + "]", jobRepository)
                .allowStartIfComplete(true).<Object, List<String>>chunk(1, transactionManager)
                .reader(new ReadRemoteTable(jdbcTemplateMap.get(region), remoteSource))
                .writer(new CreateRemoteSubscription(jdbcTemplateMap.get(region), remoteSource, region)).build();
        Properties properties = ciPropertiesMap.get(region);
        Step unDeployArtifactStep = new StepBuilder("Undeploy Artifacts [" + regionProperties.getProperty(region) + "]", jobRepository)
                .allowStartIfComplete(true)
                .tasklet(new UndeployArtifacts(properties, ciPackageIds, region), transactionManager).build();
        String replicationRole = "standby";
        properties = aemPropertiesMap.get(region);
        Step aemReplicationStandby = new StepBuilder(
                "AEM Replication Role Change [" + regionProperties.getProperty(region) + "] [" + replicationRole + "]", jobRepository)
                .allowStartIfComplete(true)
                .tasklet(new ReplicationRoleChange(properties, replicationRole, region), transactionManager).build();

        Job job = new JobBuilder("Region Recovered [" + regionProperties.getProperty(region) + "]", jobRepository).incrementer(new RunIdIncrementer())
                .start(createSubscriptionStep).next(unDeployArtifactStep).next(aemReplicationStandby).build();
        JobExecution execution = jobLauncher.run(job, new JobParameters());
        Long executionId = execution.getId();
        insertNotes(executionId, "resolved", "Region Recovered");
        return executionId;
    }

    public Long switchRegion(String region, Long routingPriority, boolean monitor, String notes) throws Exception {
        String alternateRegion = region.equals("primary") ? "secondary" : "primary";
        Step replicationMonitorStep = new StepBuilder("Monitor Replication [" + regionProperties.getProperty(region) + "]",jobRepository).allowStartIfComplete(true)
        .tasklet(new ReplicationMonitor(aemPropertiesMap.get(region), jdbcTemplateMap.get(region), region), transactionManager).build();

        Step tmRoutingPriorityChangeStep = new StepBuilder("Routing Priority Change [" + routingPriority + "]",
                jobRepository).allowStartIfComplete(true)
                .tasklet(new TMPriorityChange(azureProperties, routingPriority), transactionManager).build();
        Step deleteSubscriptionStep = new StepBuilder("Delete Subscription [" + regionProperties.getProperty(region) + "]", jobRepository)
                .allowStartIfComplete(true).<Object, List<String>>chunk(1, transactionManager)
                .reader(new ReadRemoteSubscription(jdbcTemplateMap.get(region), remoteSource))
                .writer(new DeleteRemoteSubscription(jdbcTemplateMap.get(region), remoteSource, region)).build();
        // Step createSubscriptionStep = new StepBuilder("Create Subscription [" + regionProperties.getProperty(alternateRegion) + "]", jobRepository)
        //         .allowStartIfComplete(true).<Object, List<String>>chunk(1, transactionManager)
        //         .reader(new ReadRemoteTable(jdbcTemplateMap.get(alternateRegion), remoteSource))
        //         .writer(new CreateRemoteSubscription(jdbcTemplateMap.get(alternateRegion), remoteSource,
        //                 alternateRegion))
        //         .build();

        Properties properties = aemPropertiesMap.get(alternateRegion);
        String replicationRole = "standby";
        Step aemReplicationRoleStandbyStep = new StepBuilder(
                "AEM Replication Role Change [" + regionProperties.getProperty(alternateRegion) + "] [" + replicationRole + "]", jobRepository)
                .allowStartIfComplete(true)
                .tasklet(new ReplicationRoleChange(properties, replicationRole, alternateRegion), transactionManager)
                .build();
        properties = aemPropertiesMap.get(region);
        replicationRole = "active";
        Step aemReplicationRoleActiveStep = new StepBuilder(
                "AEM Replication Role Change [" + regionProperties.getProperty(region) + "] [" + replicationRole + "]", jobRepository)
                .allowStartIfComplete(true)
                .tasklet(new ReplicationRoleChange(properties, replicationRole, region), transactionManager).build();

        properties = ciPropertiesMap.get(alternateRegion);
        Step unDeployArtifactStep = new StepBuilder("Undeploy Artifacts [" + regionProperties.getProperty(alternateRegion) + "]", jobRepository)
                .allowStartIfComplete(true)
                .tasklet(new UndeployArtifacts(properties, ciPackageIds, alternateRegion), transactionManager).build();
        properties = ciPropertiesMap.get(region);
        Step deployArtifactStep = new StepBuilder("Deploy Artifacts [" + regionProperties.getProperty(region) + "]", jobRepository)
                .allowStartIfComplete(true)
                .tasklet(new DeployArtifacts(properties, ciPackageIds, region), transactionManager).build();
        Job job = null;

        if(monitor) {
                // job = new JobBuilder("Region Switch [" + regionProperties.getProperty(alternateRegion) + " -> " + regionProperties.getProperty(region) + "]", jobRepository)
                // .listener(listener).start(replicationMonitorStep).next(tmRoutingPriorityChangeStep).next(deleteSubscriptionStep)
                // .next(aemReplicationRoleStandbyStep)
                // .next(aemReplicationRoleActiveStep).build();

                job = new JobBuilder("Region Switch [" + regionProperties.getProperty(alternateRegion) + " -> " + regionProperties.getProperty(region) + "]", jobRepository)
                .listener(listener).start(replicationMonitorStep).next(tmRoutingPriorityChangeStep).next(deleteSubscriptionStep)
                .next(unDeployArtifactStep).next(aemReplicationRoleStandbyStep)
                .next(aemReplicationRoleActiveStep).next(deployArtifactStep).build();
        }    else {
                // job = new JobBuilder("Region Switch [" + regionProperties.getProperty(alternateRegion) + " -> " + regionProperties.getProperty(region) + "]", jobRepository)
                // .listener(listener).start(tmRoutingPriorityChangeStep).next(deleteSubscriptionStep)
                // .next(aemReplicationRoleStandbyStep)
                // .next(aemReplicationRoleActiveStep).build();

                job = new JobBuilder("Region Switch [" + regionProperties.getProperty(alternateRegion) + " -> " + regionProperties.getProperty(region) + "]", jobRepository)
                .listener(listener).start(tmRoutingPriorityChangeStep).next(deleteSubscriptionStep)
                .next(unDeployArtifactStep).next(aemReplicationRoleStandbyStep)
                .next(aemReplicationRoleActiveStep).next(deployArtifactStep).build();
        } 

        if (!jobRegistry.getJobNames().contains(job.getName())) {
            jobRegistry.register(new ReferenceJobFactory(job));
        }
        JobExecution execution = jobLauncher.run(job, new JobParameters());
        Long executionId = execution.getId();
        insertNotes(executionId, "switch", notes);
        return executionId;
    }

    public Long hanaRemoteSubscription(String region, String action, String notes) throws Exception {
        if (action.equals("delete")) {
            return deleteSubscription(region,notes);
        } else {
            return createSubscription(region,notes);
        }
    }

    public Long ciArtifacts(String region, String[] packageIds, String action, String notes) throws Exception {
        if (action.equals("deploy")) {
            return deployArtifacts(region, packageIds, notes);
        } else {
            return unDeployArtifacts(region,packageIds, notes);
        }
    }

    private Long createSubscription(String region, String notes) throws Exception {
        Step createSubscriptionStep = new StepBuilder("Create Subscription [" + regionProperties.getProperty(region) + "]", jobRepository)
                .allowStartIfComplete(true).<Object, List<String>>chunk(1, transactionManager)
                .reader(new ReadRemoteTable(jdbcTemplateMap.get(region), remoteSource))
                .writer(new CreateRemoteSubscription(jdbcTemplateMap.get(region), remoteSource, region)).build();
        Job job = new JobBuilder("Create Subscription [" + regionProperties.getProperty(region) + "]", jobRepository)
                .incrementer(new RunIdIncrementer()).start(createSubscriptionStep).build();
        JobExecution execution = jobLauncher.run(job, new JobParameters());
        Long executionId = execution.getId();
        insertNotes(executionId, "createSubscription", notes);
        return executionId;
    }

    private Long deleteSubscription(String region, String notes) throws Exception {
        Step deleteSubscriptionStep = new StepBuilder("Delete Subscription [" + regionProperties.getProperty(region) + "]", jobRepository)
                .allowStartIfComplete(true).<Object, List<String>>chunk(1, transactionManager)
                .reader(new ReadRemoteSubscription(jdbcTemplateMap.get(region), remoteSource))
                .writer(new DeleteRemoteSubscription(jdbcTemplateMap.get(region), remoteSource, region)).build();
        Job job = new JobBuilder("Delete Subscription [" + regionProperties.getProperty(region) + "]", jobRepository)
                .incrementer(new RunIdIncrementer()).start(deleteSubscriptionStep).build();
        JobExecution execution = jobLauncher.run(job, new JobParameters());
        Long executionId = execution.getId();
        insertNotes(executionId, "deleteSubscription", notes);
        return executionId;
    }

    private Long deployArtifacts(String region, String[] packageIds, String notes) throws Exception {
        Properties properties = ciPropertiesMap.get(region);
        Step deployArtifactStep = new StepBuilder("Deploy Artifacts [" + regionProperties.getProperty(region) + "]", jobRepository)
                .allowStartIfComplete(true)
                .tasklet(new DeployArtifacts(properties, packageIds, region), transactionManager).build();
        Job job = new JobBuilder("Deploy Artifacts [" + regionProperties.getProperty(region) + "]", jobRepository).incrementer(new RunIdIncrementer())
                .start(deployArtifactStep).build();
        JobExecution execution = jobLauncher.run(job, new JobParameters());
        Long executionId = execution.getId();
        insertNotes(executionId, "deployArtifacts", notes);
        return executionId;
    }

    private Long unDeployArtifacts(String region, String[] packageIds, String notes) throws Exception {
        Properties properties = ciPropertiesMap.get(region);
        Step unDeployArtifactStep = new StepBuilder("Undeploy Artifacts [" + regionProperties.getProperty(region) + "]", jobRepository)
                .allowStartIfComplete(true)
                .tasklet(new UndeployArtifacts(properties, packageIds, region), transactionManager).build();
        Job job = new JobBuilder("Undeploy Artifacts [" + regionProperties.getProperty(region) + "]", jobRepository)
                .incrementer(new RunIdIncrementer()).start(unDeployArtifactStep).build();
        JobExecution execution = jobLauncher.run(job, new JobParameters());
        Long executionId = execution.getId();
        insertNotes(executionId, "unDeployArtifacts", notes);
        return executionId;
    }

    public Long aemReplicationRoleChange(String region, String replicationRole, String notes) throws Exception {
        Properties properties = aemPropertiesMap.get(region);
        Step aemReplicationRoleChangeStep = new StepBuilder(
                "AEM Replication Role Change [" + regionProperties.getProperty(region) + "] [" + replicationRole + "]", jobRepository)
                .allowStartIfComplete(true)
                .tasklet(new ReplicationRoleChange(properties, replicationRole, region), transactionManager).build();
        Job job = new JobBuilder("AEM Replication Role Change [" + regionProperties.getProperty(region) + "] [" + replicationRole + "]",
                jobRepository).incrementer(new RunIdIncrementer()).start(aemReplicationRoleChangeStep).build();
        JobExecution execution = jobLauncher.run(job, new JobParameters());
        Long executionId = execution.getId();
        insertNotes(executionId, "aemReplicationRoleChange", notes);
        return executionId;
    }

    public Long tmRoutingPriority(Long routingPriority, String notes) throws Exception {
        Step tmRoutingPriorityChangeStep = new StepBuilder("Routing Priority Change [" + routingPriority + "]",
                jobRepository).allowStartIfComplete(true)
                .tasklet(new TMPriorityChange(azureProperties, routingPriority), transactionManager).build();
        Job job = new JobBuilder("Routing Priority Change [" + routingPriority + "]", jobRepository)
                .incrementer(new RunIdIncrementer()).start(tmRoutingPriorityChangeStep).build();
        JobExecution execution = jobLauncher.run(job, new JobParameters());
        Long executionId = execution.getId();
        insertNotes(executionId, "tmRoutingPriority", notes);
        return executionId;
    }

    public Long tmAllRoutingChange(String trafficStatus, String notes) throws Exception {
        String region = "primary";
        Step tmRoutingStatusChangeStepPrimary = new StepBuilder(
                "Traffic Routing Status Change  [" + regionProperties.getProperty(region) + "] [" + trafficStatus + "]", jobRepository)
                .allowStartIfComplete(true)
                .tasklet(new TMRoutingChange(azureProperties, trafficStatus, region), transactionManager).build();
        region = "secondary";
        Step tmRoutingStatusChangeStepSecondary = new StepBuilder(
                "Traffic Routing Status Change  [" + regionProperties.getProperty(region) + "] [" + trafficStatus + "]", jobRepository)
                .allowStartIfComplete(true)
                .tasklet(new TMRoutingChange(azureProperties, trafficStatus, region), transactionManager).build();
        Job job = new JobBuilder("Traffic Routing Status Change [All]  [" + trafficStatus + "]", jobRepository)
                .incrementer(new RunIdIncrementer()).start(tmRoutingStatusChangeStepPrimary)
                .next(tmRoutingStatusChangeStepSecondary).build();
        JobExecution execution = jobLauncher.run(job, new JobParameters());
        Long executionId = execution.getId();
        insertNotes(executionId, "tmAllRoutingChange", notes);
        return executionId;
    }

    public Long tmRoutingChange(String region, String trafficStatus, String notes) throws Exception {
        Step tmRoutingStatusChangeStep = new StepBuilder("Traffic Routing Status Change [" + trafficStatus + "]",
                jobRepository).allowStartIfComplete(true)
                .tasklet(new TMRoutingChange(azureProperties, trafficStatus, region), transactionManager).build();
        Job job = new JobBuilder("Traffic Routing Status Change  [" + regionProperties.getProperty(region) + "] [" + trafficStatus + "]",
                jobRepository).incrementer(new RunIdIncrementer()).start(tmRoutingStatusChangeStep).build();
        JobExecution execution = jobLauncher.run(job, new JobParameters());
        Long executionId = execution.getId();
        insertNotes(executionId, "tmRoutingChange", notes);
        return executionId;
    }

    public Long stopAllOperations(String notes) throws Exception {
        String trafficStatus = "disabled";
        String replicationRole = "standby";
        String region = "primary";
        Properties properties = aemPropertiesMap.get(region);
        Step tmRoutingStatusChangeStepPrimary = new StepBuilder(
                "Traffic Routing Status Change [" + regionProperties.getProperty(region) + "] [" + trafficStatus + "]", jobRepository)
                .allowStartIfComplete(true)
                .tasklet(new TMRoutingChange(azureProperties, trafficStatus, region), transactionManager).build();
        Step aemReplicationRoleChangeStepPrimary = new StepBuilder(
                "AEM Replication Role Change [" + regionProperties.getProperty(region) + "] [" + replicationRole + "]", jobRepository)
                .allowStartIfComplete(true)
                .tasklet(new ReplicationRoleChange(properties, replicationRole, region), transactionManager).build();
        region = "secondary";
        properties = aemPropertiesMap.get(region);
        Step tmRoutingStatusChangeStepSecondary = new StepBuilder(
                "Traffic Routing Status Change [" + regionProperties.getProperty(region) + "] [" + trafficStatus + "]", jobRepository)
                .allowStartIfComplete(true)
                .tasklet(new TMRoutingChange(azureProperties, trafficStatus, region), transactionManager).build();
        Step aemReplicationRoleChangeStepSecondary = new StepBuilder(
                "AEM Replication Role Change [" + regionProperties.getProperty(region) + "] [" + replicationRole + "]", jobRepository)
                .allowStartIfComplete(true)
                .tasklet(new ReplicationRoleChange(properties, replicationRole, region), transactionManager).build();

        Job job = new JobBuilder("Stop Operations", jobRepository).incrementer(new RunIdIncrementer())
                .start(tmRoutingStatusChangeStepPrimary).next(tmRoutingStatusChangeStepSecondary)
                .next(aemReplicationRoleChangeStepPrimary).next(aemReplicationRoleChangeStepSecondary).build();
        JobExecution execution = jobLauncher.run(job, new JobParameters());
        Long executionId = execution.getId();
        insertNotes(executionId, "stopAllOperations", notes);
        return executionId;
    }

    public Long startAllOperations(String notes) throws Exception {
        String trafficStatus = "enabled";
        String region = "primary";
        Step tmRoutingStatusChangeStepPrimary = new StepBuilder(
                "Traffic Routing Status Change [" + regionProperties.getProperty(region) + "] [" + trafficStatus + "]", jobRepository)
                .allowStartIfComplete(true)
                .tasklet(new TMRoutingChange(azureProperties, trafficStatus, region), transactionManager).build();
        region = "secondary";
        Step tmRoutingStatusChangeStepSecondary = new StepBuilder(
                "Traffic Routing Status Change [" + regionProperties.getProperty(region) + "] [" + trafficStatus + "]", jobRepository)
                .allowStartIfComplete(true)
                .tasklet(new TMRoutingChange(azureProperties, trafficStatus, region), transactionManager).build();
        Job job = new JobBuilder("Start Operations", jobRepository).incrementer(new RunIdIncrementer())
                .start(tmRoutingStatusChangeStepPrimary).next(tmRoutingStatusChangeStepSecondary).build();
        JobExecution execution = jobLauncher.run(job, new JobParameters());
        Long executionId = execution.getId();
        insertNotes(executionId, "startAllOperations", notes);
        return executionId;
    }

    public Long aemAllStandby(String notes) throws Exception {
        String replicationRole = "standby";
        String region = "primary";
        Properties properties = aemPropertiesMap.get(region);
        Step aemReplicationRoleChangeStepPrimary = new StepBuilder(
                "AEM Replication Role Change [" + regionProperties.getProperty(region) + "] [" + replicationRole + "]", jobRepository)
                .allowStartIfComplete(true)
                .tasklet(new ReplicationRoleChange(properties, replicationRole, region), transactionManager).build();
        region = "secondary";
        properties = aemPropertiesMap.get(region);
        Step aemReplicationRoleChangeStepSecondary = new StepBuilder(
                "AEM Replication Role Change [" + regionProperties.getProperty(region) + "] [" + replicationRole + "]", jobRepository)
                .allowStartIfComplete(true)
                .tasklet(new ReplicationRoleChange(properties, replicationRole, region), transactionManager).build();

        Job job = new JobBuilder("AEM All Regions to Standby", jobRepository).incrementer(new RunIdIncrementer())
                .start(aemReplicationRoleChangeStepPrimary).next(aemReplicationRoleChangeStepSecondary).build();
        JobExecution execution = jobLauncher.run(job, new JobParameters());
        Long executionId = execution.getId();
        insertNotes(executionId, "aemAllStandby", notes);
        return executionId;
    }
    public Long replicationMonitor(String region) throws Exception {
        Step replicationMonitorStep = new StepBuilder("Monitor Replication [" + regionProperties.getProperty(region) + "]",
                jobRepository).allowStartIfComplete(true)
                               .tasklet(new ReplicationMonitor(aemPropertiesMap.get(region), jdbcTemplateMap.get(region), region), transactionManager).build();
        Job job = new JobBuilder("Monitor Replication  [" + regionProperties.getProperty(region) + "]",
                jobRepository).incrementer(new RunIdIncrementer()).start(replicationMonitorStep).build();
        JobExecution execution = jobLauncher.run(job, new JobParameters());
        return execution.getId();
    }
    public boolean stopJob(Long jobId) throws Exception {
        return jobOperator.stop(jobId);
    }

    public Long restartJob(Long jobId) throws Exception {
        Long restartId = jobOperator.restart(jobId);
        JobExecution jobExecution = jobExplorer.getJobExecution(jobId);
        jobExecution.setExitStatus(new ExitStatus("RESTARTED", "Restarted with Job Id: [" + restartId + "]"));
        jobRepository.update(jobExecution);
        return restartId;
    }

     private void insertNotes(Long executionId, String executionType, String notes) throws Exception {
           String query = "INSERT INTO BATCH_JOB_EXECUTION_NOTES VALUES("+executionId+",\'"+executionType+"\',"+performanceTrace+",\'"+performanceTraceRegion+"\',\'"+notes+"\');";
           jdbcTemplate.update(query);
     }

}
