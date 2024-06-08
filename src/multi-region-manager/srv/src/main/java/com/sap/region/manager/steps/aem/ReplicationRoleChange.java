package com.sap.region.manager.steps.aem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.Properties;

/**
 * <p>
 * ReplicationRoleChange class.
 * </p>
 *
 * @author shanthakumar.krishnaswamy@sap.com
 */

public class ReplicationRoleChange implements Tasklet {
    private static final Logger logger = LoggerFactory.getLogger(ReplicationRoleChange.class);
    private Properties properties;
    private String replicationRole;
    private String region;

    public ReplicationRoleChange(Properties properties, String replicationRole, String region) {
        this.properties = properties;
        this.replicationRole = replicationRole;
        this.region = region;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL))
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        try {
            logger.info("Replication role change started : [Role:" + replicationRole + ", Region:" + region + "]");
            String jsonPayload = "{\"replicationRole\": \"" + replicationRole + "\"}";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(
                            properties.getProperty("url") + "/SEMP/v2/config/msgVpns/" + properties.getProperty("vpn")))
                    .headers("Authorization", Helper.getBasicAuthenticationHeader(properties), "content-type",
                            "application/json")
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();
            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                logger.info("Replication role change completed successfully: [Role:" + replicationRole + ", Region:"
                        + region + "]");
            } else {
                logger.error(response.body());
                throw new Exception("Error in Replication role change. [Region: " + region + ", Role:" + replicationRole
                        + "] Response code: [" + response.statusCode() + " ] Response body: [" + response.body());
            }
            contribution.setExitStatus(new ExitStatus(contribution.getExitStatus().getExitCode(),
                    "Replication role change completed successfully: [Role:" + replicationRole + ", Region:" + region
                            + "]"));
        } catch (Exception ex) {
            logger.error("Error in replication role change", ex);
            throw ex;
        }
        return RepeatStatus.FINISHED;
    }
}
