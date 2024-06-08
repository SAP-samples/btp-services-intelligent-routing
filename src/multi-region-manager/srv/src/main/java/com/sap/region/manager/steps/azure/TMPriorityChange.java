package com.sap.region.manager.steps.azure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.resourcemanager.trafficmanager.models.TrafficManagerProfile;

import java.util.Properties;

/**
 * <p>TMPriorityChange class.</p>
 *
 * @author shanthakumar.krishnaswamy@sap.com
 */
public class TMPriorityChange implements Tasklet  {
    private static final Logger logger = LoggerFactory.getLogger(TMPriorityChange.class);
    private Properties properties;
    private Long routingPriority;
    public TMPriorityChange(Properties properties, Long routingPriority) {
        this.properties = properties;
        this.routingPriority = routingPriority;
    }
    
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        try {
            AzureResourceManager azureResourceManager = getAzureResoureManager();
            TrafficManagerProfile  trafficManagerProfile  = azureResourceManager.trafficManagerProfiles().getByResourceGroup(properties.getProperty("tm_resource_group"), properties.getProperty("tm_profile_name"));
            trafficManagerProfile.update().updateExternalTargetEndpoint(properties.getProperty("tm_endpoint_primary")).withRoutingPriority(routingPriority.intValue()).parent().apply();    
            Long changedRoutingPriority = trafficManagerProfile.externalEndpoints().get(properties.getProperty("tm_endpoint_primary")).routingPriority();
            logger.info("Traffic Manager routing priority changed to: "+changedRoutingPriority);
            contribution.setExitStatus(new ExitStatus(contribution.getExitStatus().getExitCode(), "Traffic Manager routing priority changed to: "+ changedRoutingPriority));
        } catch (Exception e) {
            logger.error("Change traffic manager routing priority failed ", e);
            throw e;
        }
        return RepeatStatus.FINISHED;
    }    
    private AzureResourceManager getAzureResoureManager(){
        AzureProfile profile = new AzureProfile(properties.getProperty("tenant_id"), properties.getProperty("subscription_id"), AzureEnvironment.AZURE);
        ClientSecretCredential credential = new ClientSecretCredentialBuilder()
                                        .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
                                        .tenantId(properties.getProperty("tenant_id"))
                                        .clientId(properties.getProperty("client_id"))
                                        .clientSecret(properties.getProperty("client_secret"))
                                        .build();
        return AzureResourceManager.authenticate(credential, profile).withSubscription(properties.getProperty("subscription_id"));
    }
    
}
