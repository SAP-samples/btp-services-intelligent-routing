package com.sap.region.manager.steps.monitor;

import org.json.JSONObject;
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;

import com.sap.region.manager.model.MessageStatus;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.List;
import java.util.Properties;

/**
 * <p>
 * Replication class.
 * </p>
 *
 * @author shanthakumar.krishnaswamy@sap.com
 */

public class ReplicationMonitor implements Tasklet {
    private static final Logger logger = LoggerFactory.getLogger(ReplicationMonitor.class);
    private Properties properties;
    private JdbcTemplate jdbcTemplate;
    private String region;

    public ReplicationMonitor(Properties properties, JdbcTemplate jdbcTemplate, String region) {
        this.properties = properties;
        this.jdbcTemplate = jdbcTemplate;
        this.region = region;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        try {
            logger.info("Replication monitor started : [ Region:" + region + "]");
            MessageStatus messageStatus = getHanaReplicationStatus();
            int aemReplicationQueueStatus = getAemReplicationQueueStatus();
            if(messageStatus.getReceivedMessageCount() == messageStatus.getAppliedMessageCount() && aemReplicationQueueStatus == 0) {
                contribution.setExitStatus(new ExitStatus(contribution.getExitStatus().getExitCode(),
                    "Replication monitor completed successfully: [HanaReceivedMessageCount:" + messageStatus.getReceivedMessageCount() + ", HanaAppliedMessageCount:" + messageStatus.getAppliedMessageCount()+ ", AemReplicationQueueStatus:" + aemReplicationQueueStatus+ "]"));
                return RepeatStatus.FINISHED;
            } else {
                Thread.sleep(10000);
                contribution.setExitStatus(new ExitStatus(contribution.getExitStatus().getExitCode(),
                    "Waiting for replication to complete : [HanaReceivedMessageCount:" + messageStatus.getReceivedMessageCount() + ", HanaAppliedMessageCount:" + messageStatus.getAppliedMessageCount()+ ", AemReplicationQueueStatus:" + aemReplicationQueueStatus+ "]"));
            }
        } catch (Exception ex) {
            logger.error("Error in replication monitor", ex);
            throw ex;
        }
        return RepeatStatus.CONTINUABLE; 
    }

    public MessageStatus getHanaReplicationStatus() {
       try {
            List<MessageStatus> messageStatusList = jdbcTemplate.query("select count(RECEIVED_MESSAGE_COUNT) AS RECEIVED_MESSAGE_COUNT, count(APPLIED_MESSAGE_COUNT) AS APPLIED_MESSAGE_COUNT from \"SYS\".\"M_REMOTE_SUBSCRIPTION_STATISTICS\"",(rs, rowNum) -> new MessageStatus("",rs.getLong("RECEIVED_MESSAGE_COUNT"),rs.getLong("APPLIED_MESSAGE_COUNT")));
            return messageStatusList.get(0);
        } catch (Exception e) {
            logger.error("getReplicationStatus failed.", e);
        }
       return null;
    }  
    public int getAemReplicationQueueStatus() {
        HttpClient client = HttpClient.newBuilder()
                .cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL))
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(
                            properties.getProperty("url") + "/SEMP/v2/monitor/msgVpns/ci-vpn/queues/%23MSGVPN_REPLICATION_DATA_QUEUE?select=msgs.count"))
                    .headers("Authorization", Helper.getBasicAuthenticationHeader(properties), "content-type",
                            "application/json")
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
            return new JSONObject(response.body()).getJSONObject("collections").getJSONObject("msgs").getInt("count");
        } catch (Exception ex) {
            logger.error("Error in getting AemReplicationQueueStatus", ex);
        }
        return 1;
    }    
}
