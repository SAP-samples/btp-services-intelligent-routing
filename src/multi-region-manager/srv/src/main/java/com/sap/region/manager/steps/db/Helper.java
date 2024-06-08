
package com.sap.region.manager.steps.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

/**
 * <p>Helper class.</p>
 *
 * @author shanthakumar.krishnaswamy@sap.com
 */
public class Helper {
    private static final Logger logger = LoggerFactory.getLogger(Helper.class);

    public static boolean isReplicationCompleted(JdbcTemplate jdbcTemplate) {
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "select count(RECEIVED_MESSAGE_COUNT) AS TOTAL_RECEIVED_MESSAGE_COUNT, count(APPLIED_MESSAGE_COUNT) AS TOTAL_APPLIED_MESSAGE_COUNT from \"SYS\".\"M_REMOTE_SUBSCRIPTION_STATISTICS\"");
            for (Map<String, Object> row : rows) {
                if (row.get("TOTAL_RECEIVED_MESSAGE_COUNT") == row.get("TOTAL_APPLIED_MESSAGE_COUNT")) {
                    return true;
                }
            }
        } catch (Exception e) {
            logger.error("isReplicationCompleted failed.", e);
        }
        return false;
    }

    public static String getSchema(JdbcTemplate jdbcTemplate) {
        Connection conn = null;
        String schema = null;
        try {
            conn = jdbcTemplate.getDataSource().getConnection();
            schema = conn.getSchema();
        } catch (Exception ex) {
            logger.error("Failed getSchema", ex);
        } finally {
            try {
                conn.close();
            } catch (Exception e) {
                // handle
            }
        }
        return schema;
    }

}
