package com.sap.region.manager.steps.db;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * <p>
 * DeleteRemoteSubscription class.
 * </p>
 *
 * @author shanthakumar.krishnaswamy@sap.com
 */
public class DeleteRemoteSubscription implements ItemWriter<List<String>>, StepExecutionListener {
    private static final Logger logger = LoggerFactory.getLogger(CreateRemoteSubscription.class);
    private JdbcTemplate jdbcTemplate;
    private String remoteSource;
    private String region;
    List<String> tableList = null;

    public DeleteRemoteSubscription(JdbcTemplate jdbcTemplate, String remoteSource, String region) {
        this.jdbcTemplate = jdbcTemplate;
        this.remoteSource = remoteSource;
        this.region = region;
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
        tableList = new ArrayList<>();
    }

    @Override
    public void write(Chunk<? extends List<String>> chunk) throws Exception {
        if (chunk.size() > 0) {
            for (List<String> tables : chunk) {
                tableList.addAll(tables);
                try {
                    String query = "ALTER REMOTE SOURCE " + remoteSource + " SUSPEND CAPTURE";
                    jdbcTemplate.execute(query);
                    for (String table : tables) {
                        logger.debug("Deleting remote subscription for table: " + table);
                        query = "DROP REMOTE SUBSCRIPTION SUB_" + table;
                        jdbcTemplate.execute(query);
                        logger.info("Deleted remote subscription for table: " + table);
                    }
                } finally {
                    String query = "ALTER REMOTE SOURCE " + remoteSource + " RESUME CAPTURE";
                    jdbcTemplate.execute(query);
                }
            }
        }
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        ExitStatus exitStatus = stepExecution.getExitStatus();
        if (ExitStatus.FAILED.getExitCode().equals(exitStatus.getExitCode())) {
            String exitDescription = exitStatus.getExitDescription();
            exitStatus = new ExitStatus(exitStatus.getExitCode(),
                    "Delete remote subscription failed.  Region: [" + region + "] " + exitDescription);
        } else {
            exitStatus = new ExitStatus(exitStatus.getExitCode(),
                    "Delete remote subscription completed successfully.  Region: [" + region + "] tables: "
                            + tableList);
        }
        logger.info("=====================================================================================");
        return exitStatus;
    }

}
