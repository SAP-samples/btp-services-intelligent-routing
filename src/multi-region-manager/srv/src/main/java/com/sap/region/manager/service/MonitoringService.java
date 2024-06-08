package com.sap.region.manager.service;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.trafficmanager.models.TrafficManagerExternalEndpoint;
import com.azure.resourcemanager.trafficmanager.models.TrafficManagerProfile;
import com.sap.region.manager.model.MessageStatus;
import com.sap.region.manager.model.Monitoring;


/**
 * <p>MonitoringService class.</p>
 *
 * @author shanthakumar.krishnaswamy@sap.com
 */
@Service
public class MonitoringService {
    private Logger logger = LoggerFactory.getLogger(ApiService.class);

    @Autowired
    @Qualifier("jdbcTemplateMap")
    private Map<String, JdbcTemplate> jdbcTemplateMap;

    @Value("${spring.remotesource}")
    private String remoteSource;

    @Autowired
    @Qualifier("aemPropertiesMap")
    private Map<String, Properties> aemPropertiesMap;

    @Autowired
    @Qualifier("ciPropertiesMap")
    private Map<String, Properties> ciPropertiesMap;

    @Autowired
    @Qualifier("azureProperties")
    private Properties azureProperties;
    
    @Autowired
    @Qualifier("regionProperties")
    private Properties regionProperties;

    public List<Monitoring> getServiceStatus() {
        List<Monitoring> list = new ArrayList<>();
        Monitoring tmStatus = new Monitoring();
        tmStatus.setServiceKey("tm");
        tmStatus.setServiceName("Azure Traffic Manager");
        Map<String,String> tmStatusMap = getTMStatus();
        String tmActiveRegion = tmStatusMap.get("activeRegion");
        String disabledRegion = tmStatusMap.get("disabledRegion");
        if(disabledRegion.equals("both")) {
            tmStatus.setComments("All external traffic suspended, since both regions are disabled");
            setActiveRegion("",tmStatus);
            tmStatus.setStatus("Warning");
        }else {
            setActiveRegion(tmActiveRegion,tmStatus);
        }
        list.add(tmStatus);
        Monitoring aemStatus = new Monitoring();
        aemStatus.setServiceKey("aem");
        aemStatus.setServiceName("SAP Advanced Event Mesh");
        String aemActiveRegion = getAemActiveRegion();
        setActiveRegion(aemActiveRegion,aemStatus);
        if(!aemActiveRegion.equals("")) {
            aemStatus.setComments(getAemReplicationQueueStatus(tmActiveRegion));
        } else {
            aemStatus.setComments("There is no active region");
            aemStatus.setStatus("Warning");
        }
        list.add(aemStatus);
        Monitoring sdaStatus = new Monitoring();
        sdaStatus.setServiceKey("sda");
        sdaStatus.setServiceName("SAP Hana Cloud");
        String hanaActiveRegion = getHanaActiveRegion();
        if(hanaActiveRegion.equals("")) { 
            sdaStatus.setComments("Replication not enabled");
            sdaStatus.setStatus("Warning");
            setActiveRegion(tmActiveRegion,sdaStatus);
        } else {
            setActiveRegion(hanaActiveRegion,sdaStatus);
            if(hanaActiveRegion.equalsIgnoreCase("primary")){
                sdaStatus.setComments(getHanaReplicationStatus("secondary"));
            }else {
                sdaStatus.setComments(getHanaReplicationStatus("primary"));
            }
        }
        list.add(sdaStatus);
        Monitoring ciStatus = new Monitoring();
        ciStatus.setServiceKey("ci");
        ciStatus.setServiceName("SAP Cloud Integration");
        setActiveRegion(tmActiveRegion,ciStatus);
        ciStatus.setComments(getCIProcessingArtifacts(tmActiveRegion));
        list.add(ciStatus);
        return list;
    }

    public Map<String, String> getTMStatus() {
        Map<String, String> status = new HashMap<String, String>();
        try {
           
            AzureResourceManager azureResourceManager = getAzureResoureManager(azureProperties);
            TrafficManagerProfile  trafficManagerProfile  = azureResourceManager.trafficManagerProfiles().getByResourceGroup(azureProperties.getProperty("tm_resource_group"), azureProperties.getProperty("tm_profile_name"));
            Map<String, TrafficManagerExternalEndpoint > externalEndpoints = trafficManagerProfile.externalEndpoints();
            TrafficManagerExternalEndpoint primaryEndpoint = externalEndpoints.get(azureProperties.getProperty("tm_endpoint_primary"));
            TrafficManagerExternalEndpoint secondaryEndpoint = externalEndpoints.get(azureProperties.getProperty("tm_endpoint_secondary"));
            
            if(primaryEndpoint.routingPriority() > 1){
                status.put("activeRegion", "secondary");
            } else {
                status.put("activeRegion", "primary");
            }

            List<String> disabledRegion = new ArrayList<>();
            if(!primaryEndpoint.isEnabled())
                disabledRegion.add("primary");
                status.put("disabledRegion", "primary");
            if(!secondaryEndpoint.isEnabled())
                disabledRegion.add("secondary");
                status.put("disabledRegion", "secondary");
            if(disabledRegion.size() == 2){
                status.put("disabledRegion", "both");
            } 
        }catch(Exception ex) {
            ex.printStackTrace();
        }
        return status;
    }

    public String getCIProcessingArtifacts(String region) {
        Properties properties = ciPropertiesMap.get(region);
        HttpClient client = HttpClient.newBuilder()
                .cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL))
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(properties.getProperty("url") + "/api/v1/MessageProcessingLogs?"+URLEncoder.encode("$format","UTF-8")+"=json&"+URLEncoder.encode("$filter","UTF-8")+"="+URLEncoder.encode("Status eq 'PROCESSING'","UTF-8")))
                    .headers("Authorization", getBasicAuthenticationHeader(properties), "content-type",
                            "application/json")
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
            int count = new JSONObject(response.body()).getJSONObject("d").getJSONArray("results").length();
            return "Running Artifacts: "+ count;
        } catch (Exception ex) {
            logger.error("Error in getting CIProcessingStatus", ex);
        }
        return null;
    } 
    public String getHanaActiveRegion() {
        try {
             JdbcTemplate  jdbcTemplate = jdbcTemplateMap.get("primary");
             Integer primarySubscriptions = jdbcTemplate.queryForObject("select count(SUBSCRIPTION_NAME) AS SUBSCRIPTION_NAME_COUNT from \"SYS\".\"M_REMOTE_SUBSCRIPTION_STATISTICS\"",Integer.class);
             jdbcTemplate = jdbcTemplateMap.get("secondary");
             Integer secondarySubscriptions = jdbcTemplate.queryForObject("select count(SUBSCRIPTION_NAME) AS SUBSCRIPTION_NAME_COUNT from \"SYS\".\"M_REMOTE_SUBSCRIPTION_STATISTICS\"",Integer.class);
             if(primarySubscriptions > secondarySubscriptions) {
                return "secondary";
             }else if(primarySubscriptions < secondarySubscriptions){
                return "primary";
             } else {
                return "";
             }
         } catch (Exception e) {
             logger.error("getHanaReplicationRegion failed.", e);
         }
        return "";
     } 

    public String getHanaReplicationStatus(String region) {
        try {
             JdbcTemplate  jdbcTemplate = jdbcTemplateMap.get(region);
             List<MessageStatus> messageStatusList = jdbcTemplate.query("select count(RECEIVED_MESSAGE_COUNT) AS RECEIVED_MESSAGE_COUNT, count(APPLIED_MESSAGE_COUNT) AS APPLIED_MESSAGE_COUNT from \"SYS\".\"M_REMOTE_SUBSCRIPTION_STATISTICS\"",(rs, rowNum) -> new MessageStatus("",rs.getLong("RECEIVED_MESSAGE_COUNT"),rs.getLong("APPLIED_MESSAGE_COUNT")));
             MessageStatus messageStatus = messageStatusList.get(0);

             return "Replication Pending Messages: "+ (messageStatus.getReceivedMessageCount() - messageStatus.getAppliedMessageCount());
         } catch (Exception e) {
             logger.error("getReplicationStatus failed.", e);
         }
        return "Unknown";
     }  
     public String getAemActiveRegion() {
            String replicationRolePrimary = getAemReplicationRole("primary");
            if(replicationRolePrimary.equals("active")) {
                return "primary";
            } else {
                String replicationRoleSecondary = getAemReplicationRole("secondary");
                if(replicationRoleSecondary .equals("active")) {
                    return "secondary";
                } else {
                    return "";
                }
            }
    } 

    public String getAemReplicationRole(String region) {
        Properties properties = aemPropertiesMap.get(region);
        HttpClient client = HttpClient.newBuilder()
                .cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL))
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(
                            properties.getProperty("url") + "/SEMP/v2/monitor/msgVpns/"+properties.getProperty("vpn")+""))
                    .headers("Authorization", getBasicAuthenticationHeader(properties), "content-type",
                            "application/json")
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
            return new JSONObject(response.body()).getJSONObject("data").getString("replicationRole");
        } catch (Exception ex) {
            logger.error("Error in getting getAemActiveRegion", ex);
        }
        return "";
    }
     public String getAemReplicationQueueStatus(String region) {
         Properties properties = aemPropertiesMap.get(region);
         HttpClient client = HttpClient.newBuilder()
                 .cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL))
                 .connectTimeout(Duration.ofSeconds(10))
                 .build();
         try {
             HttpRequest request = HttpRequest.newBuilder()
                     .uri(new URI(
                             properties.getProperty("url") + "/SEMP/v2/monitor/msgVpns/ci-vpn/queues/%23MSGVPN_REPLICATION_DATA_QUEUE?select=msgs.count"))
                     .headers("Authorization", getBasicAuthenticationHeader(properties), "content-type",
                             "application/json")
                     .GET()
                     .build();
             HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
             return "Replication Pending Messages: "+ new JSONObject(response.body()).getJSONObject("collections").getJSONObject("msgs").getInt("count");
         } catch (Exception ex) {
             logger.error("Error in getting AemReplicationQueueStatus", ex);
         }
         return "Unknown";
     }  

    private AzureResourceManager getAzureResoureManager(Properties properties){
        AzureProfile profile = new AzureProfile(properties.getProperty("tenant_id"), properties.getProperty("subscription_id"), AzureEnvironment.AZURE);
        ClientSecretCredential credential = new ClientSecretCredentialBuilder()
                                        .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
                                        .tenantId(properties.getProperty("tenant_id"))
                                        .clientId(properties.getProperty("client_id"))
                                        .clientSecret(properties.getProperty("client_secret"))
                                        .build();
        return AzureResourceManager.authenticate(credential, profile).withSubscription(properties.getProperty("subscription_id"));
    }
    
    private void setActiveRegion(String region, Monitoring monitoring) {
        if(region.equals("")) {
            monitoring.setActiveRegionKey("");
            monitoring.setActiveRegionName("");
            monitoring.setStandByRegionKey("both");
            monitoring.setStandByRegionName(regionProperties.getProperty("primary")+","+regionProperties.getProperty("secondary"));    
        } else {
            monitoring.setActiveRegionKey(region);
            monitoring.setActiveRegionName(regionProperties.getProperty(region));
            String alternateRegion = region.equals("primary") ? "secondary" : "primary";
            monitoring.setStandByRegionKey(alternateRegion);
            monitoring.setStandByRegionName(regionProperties.getProperty(alternateRegion));
        }
    }
        
    private String getBasicAuthenticationHeader(Properties properties) {
        String valueToEncode = properties.getProperty("username") + ":" + properties.getProperty("password");
        return "Basic " + Base64.getEncoder().encodeToString(valueToEncode.getBytes());
    }
}
