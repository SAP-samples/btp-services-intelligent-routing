package com.sap.region.manager.config;


import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * <p>BatchConfig class.</p>
 *
 * @author shanthakumar.krishnaswamy@sap.com
 */
@Configuration
public class BatchConfig extends DefaultBatchConfiguration {
     private static final String TABLE_PREFIX = "rtr_";

    @Override
    public JobLauncher jobLauncher() {
        TaskExecutorJobLauncher jobLauncher = new TaskExecutorJobLauncher();
        jobLauncher.setJobRepository(jobRepository());
        jobLauncher.setTaskExecutor(getTaskExecutor());
        return jobLauncher;
    }  
    @Override
    protected TaskExecutor getTaskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(10);
        executor.setThreadNamePrefix("Batch");
        executor.initialize();
        return executor;
	}

    @Bean(name = "asyncTaskExecutor")
    public AsyncTaskExecutor asyncTaskExecutor() {
        // ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // executor.setCorePoolSize(10);
        // executor.setMaxPoolSize(15);
        // executor.setQueueCapacity(50);
        return new SimpleAsyncTaskExecutor("asyncTaskExecutor");
    }
}
