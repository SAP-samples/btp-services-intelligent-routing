package com.sap.region.manager.steps.db;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemReader;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * <p>ReadRemoteTable class.</p>
 *
 * @author shanthakumar.krishnaswamy@sap.com
 */
public class ReadRemoteTable implements ItemReader<List<String>>, StepExecutionListener {
    private static final Logger logger = LoggerFactory.getLogger(ReadRemoteTable.class);
    private JdbcTemplate jdbcTemplate;
    private String remoteSource;
    List<String> tableList = null;
    private int page = 0;
    private int rowSize = 10;

    public ReadRemoteTable(JdbcTemplate jdbcTemplate, String remoteSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.remoteSource = remoteSource;
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
        tableList = new ArrayList<>();
	}
    
    @Override
    public List<String> read() throws Exception {
        int offset = rowSize * page++;
        logger.info("Reading remote tables: " + offset);
        List<String> tables = jdbcTemplate.queryForList(
                "SELECT TABLE_NAME FROM \"" + remoteSource + "\".\"SYS\".\"M_CS_TABLES\" WHERE SCHEMA_NAME = '"
                        + Helper.getSchema(jdbcTemplate) + "' limit " + rowSize + " OFFSET " + offset,
                String.class);
                tableList.addAll(tables);
        if (tables.isEmpty())
            return null;
        else
            return tables;
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        ExitStatus exitStatus = stepExecution.getExitStatus();
        if (ExitStatus.FAILED.getExitCode().equals(exitStatus.getExitCode())) {
            String exitDescription = exitStatus.getExitDescription();
            exitStatus = new ExitStatus(exitStatus.getExitCode(), "Read remote tables failed "+ exitDescription);
        } else {
            exitStatus = new ExitStatus(exitStatus.getExitCode(), "Read remote tables completed " + tableList);
        }
        logger.info("=====================================================================================");
        return exitStatus;
    }   

}
