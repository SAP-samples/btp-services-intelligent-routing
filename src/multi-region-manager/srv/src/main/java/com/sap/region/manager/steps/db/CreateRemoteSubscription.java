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
 * <p>CreateRemoteSubscription class.</p>
 *
 * @author shanthakumar.krishnaswamy@sap.com
 */
public class CreateRemoteSubscription implements ItemWriter<List<String>>, StepExecutionListener {
    private static final Logger logger = LoggerFactory.getLogger(CreateRemoteSubscription.class);
    private JdbcTemplate jdbcTemplate;
    private String remoteSource;
    List<String> tableList = null;
    private String region;
    public CreateRemoteSubscription(JdbcTemplate jdbcTemplate, String remoteSource, String region) {
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
        if(chunk.size()>0) {
            for (List<String> tables : chunk) {
                tableList.addAll(tables);
                
                for (String table : tables) {
                    logger.debug("Creating subscription for table: " + table);
                    String query = "SELECT count(*) from \"SYS\".\"REMOTE_SUBSCRIPTIONS\" WHERE TARGET_OBJECT_NAME = '"
                            + table + "'";
                    Number number = jdbcTemplate.queryForObject(query, Integer.class);
                    if (number.intValue() > 0) {
                        logger.info("Subscription already exists: " + table);
                    } else {
                        //Recreate the Virtual table, if already exists, since schema update is possible
                        try {
                            query = "DROP TABLE VT_" + table;
                            jdbcTemplate.execute(query);
                        } catch(Exception ex){
                            //virtual table not exists
                        }
                        query = "CREATE VIRTUAL TABLE VT_" + table + " AT \""+ remoteSource +"\".\"<NULL>\".\"CI_TECH_USER\".\"" + table + "\"";
                        jdbcTemplate.execute(query);
                        //Recreate the Replica table if already exists, since schema update is possible
                        try {
                            query = "DROP TABLE " + table;
                            jdbcTemplate.execute(query);
                        } catch(Exception ex){
                            //Replica table not exists
                        }
                        query = "CREATE COLUMN TABLE " + table + " LIKE VT_" + table;
                        jdbcTemplate.execute(query);
                        query = "CREATE REMOTE SUBSCRIPTION SUB_" + table + " ON VT_" + table + " TARGET TABLE " + table;
                        jdbcTemplate.execute(query);
                        query = "ALTER REMOTE SUBSCRIPTION SUB_" + table + " DISTRIBUTE";
                        jdbcTemplate.execute(query);
                        logger.info("Created subscription for table: " + table);
                    }
                }
            }
        }
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        ExitStatus exitStatus = stepExecution.getExitStatus();
        if (ExitStatus.FAILED.getExitCode().equals(exitStatus.getExitCode())) {
            String exitDescription = exitStatus.getExitDescription();
            exitStatus = new ExitStatus(exitStatus.getExitCode(), "Create remote subscription failed.  Region: ["+ region +"] " + exitDescription);
        } else {
            exitStatus = new ExitStatus(exitStatus.getExitCode(), "Create remote subscription completed successfully.  Region: ["+ region +"] tables: " + tableList);
        }
        logger.info("=====================================================================================");
        return exitStatus;
    }   

}
