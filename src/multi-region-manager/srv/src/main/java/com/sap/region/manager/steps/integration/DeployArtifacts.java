/**
 * @author : Shanthakumar K
 * @mailto : shanthakumar.krishnaswamy@sap.com
 * @created : 07-04-2024, Sunday
 **/
package com.sap.region.manager.steps.integration;

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
import java.util.List;
import java.util.Properties;

/**
 * <p>
 * DeployArtifacts class.
 * </p>
 *
 * @author shanthakumar.krishnaswamy@sap.com
 */

public class DeployArtifacts implements Tasklet {
    private static final Logger logger = LoggerFactory.getLogger(DeployArtifacts.class);
    private Properties properties;
    private String[] packageIds;
    private String region;

    public DeployArtifacts(Properties properties, String[] packageIds, String region) {
        this.properties = properties;
        this.packageIds = packageIds;
        this.region = region;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL))
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        for (String packageId : packageIds) {
            List<String> designtimeArtifactIds = Helper.getDesigntimeArtifactIds(packageId, properties, client);
            try {
                String csrfToken = Helper.getCsrfToken(properties, client);
                for (String designtimeArtifactId : designtimeArtifactIds) {
                    // results.send("Deployment started for artifact Id: " + runtimeArtifactId);
                    logger.info("Deployment started for artifact Id: " + designtimeArtifactId);
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(new URI(
                                    properties.getProperty("url") + "/api/v1/DeployIntegrationDesigntimeArtifact?Id='"
                                            + designtimeArtifactId + "'&Version='active'"))
                            .headers("Authorization", Helper.getBasicAuthenticationHeader(properties), "Accept",
                                    "application/json", "X-CSRF-Token", csrfToken)
                            .POST(HttpRequest.BodyPublishers.noBody())
                            .build();
                    HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
                    if (response.statusCode() == 202) {
                        logger.info("Deployment successfully completed for artifact Id: " + designtimeArtifactId);
                    } else {
                        logger.error(response.body());
                        throw new Exception("Error in deploying artifacts. Region: [" + region + "] Response code: ["
                                + response.statusCode() + " ] Response body: [" + response.body());
                    }
                }
                contribution.setExitStatus(new ExitStatus(contribution.getExitStatus().getExitCode(),
                        "Deployment successfully completed. Region: [" + region + "] Artifact Id: "
                                + designtimeArtifactIds));
            } catch (Exception ex) {
                logger.error("Error in undeploying artifacts", ex);
                throw ex;
            }
        }
        return RepeatStatus.FINISHED;
    }
}
