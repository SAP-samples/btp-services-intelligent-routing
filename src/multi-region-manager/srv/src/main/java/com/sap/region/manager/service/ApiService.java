package com.sap.region.manager.service;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.sql.Connection;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.sap.region.manager.model.Config;
import com.sap.region.manager.model.MessageStatus;
import com.sap.region.manager.model.Pair;
import com.sap.region.manager.model.ReplicationStatus;

/**
 * <p>ApiService class.</p>
 *
 * @author shanthakumar.krishnaswamy@sap.com
 */
@Service
public class ApiService {
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
    @Qualifier("regionProperties")
    private Properties regionProperties;

    @Value("${PERFORMANCE_TRACE:false}")
    private boolean performanceTrace;

    public Config getConfig() {
        Config config = new Config();
        List<Pair> regionNameList = new ArrayList<>();
        regionNameList.add(new Pair("primary", regionProperties.getProperty("primary")));
        regionNameList.add(new Pair("secondary", regionProperties.getProperty("secondary")));
        config.setRegionNames(regionNameList);
        config.setPerformanceTraceEnabled(performanceTrace);
        return config;
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
            return response.body();
        } catch (Exception ex) {
            logger.error("Error in getting CIProcessingStatus", ex);
        }
        return null;
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
                            properties.getProperty("url") + "/SEMP/v2/monitor/msgVpns/"+properties.getProperty("vpn")+"/queues/%23MSGVPN_REPLICATION_DATA_QUEUE?select=msgs.count"))
                    .headers("Authorization", getBasicAuthenticationHeader(properties), "content-type",
                            "application/json")
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
            return response.body();
        } catch (Exception ex) {
            logger.error("Error in getting AemReplicationQueueStatus", ex);
        }
        return null;
    }    
    
    public String getHanaReplicationStatus(String region, int rowSize, int offset) {
        String jsonString = "";
        try {
            JdbcTemplate jdbcTemplate = jdbcTemplateMap.get(region);
            List<String> sourceTables = new ArrayList<>();
            try {
                sourceTables = jdbcTemplate.queryForList(
                        "SELECT TABLE_NAME FROM \""+ remoteSource +"\".\"SYS\".\"M_CS_TABLES\" WHERE SCHEMA_NAME = '" + getSchema(region) + "' limit " + rowSize + " OFFSET " + offset,
                        String.class);
                        
            } catch (Exception ex) {
                //remote is down
            }           
            
            List<MessageStatus> messageStatusList = jdbcTemplate.query("select SUBSCRIPTION_NAME, RECEIVED_MESSAGE_COUNT, APPLIED_MESSAGE_COUNT from \"SYS\".\"M_REMOTE_SUBSCRIPTION_STATISTICS\"",(rs, rowNum) -> new MessageStatus(rs.getString("SUBSCRIPTION_NAME"), rs.getLong("RECEIVED_MESSAGE_COUNT"),rs.getLong("APPLIED_MESSAGE_COUNT")));
            List<ReplicationStatus> replicationStatusList = new ArrayList<>();
            if(sourceTables.size()>0) {
                for(String sourceTable : sourceTables) {
                    ReplicationStatus replicationStatus = new ReplicationStatus();
                    replicationStatus.setRegion(region);
                    replicationStatus.setSourceTable(sourceTable);
                    MessageStatus messageStatus = messageStatusList.stream().filter(message -> ("SUB_"+sourceTable).equals(message.getSubscriptionName())).findAny().orElse(null);
                    if(messageStatus!=null) {
                        replicationStatus.setReplicationEnabled(true);
                        replicationStatus.setReplicationTable(sourceTable);
                        replicationStatus.setMessageStatus(messageStatus);
                    }
                    replicationStatusList.add(replicationStatus);
                }
            } else {
                for(MessageStatus messageStatus : messageStatusList) {
                    ReplicationStatus replicationStatus = new ReplicationStatus();
                    replicationStatus.setSourceTable("Unknown");
                    replicationStatus.setReplicationTable(messageStatus.getSubscriptionName().replace("SUB_", ""));
                    replicationStatus.setReplicationEnabled(true);
                    replicationStatus.setMessageStatus(messageStatus);
                    replicationStatusList.add(replicationStatus);
                }
            }        
            jsonString = new Gson().toJson(replicationStatusList);
        } catch (Exception e) {
            logger.error("getReplicationStatus failed.", e);
        }
        return jsonString;
    }    

     public String getSchema(String region) {
        Connection conn = null;
        String schema = null;
        try {
            JdbcTemplate jdbcTemplate = jdbcTemplateMap.get(region);
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
    private String getBasicAuthenticationHeader(Properties properties) {
        String valueToEncode = properties.getProperty("username") + ":" + properties.getProperty("password");
        return "Basic " + Base64.getEncoder().encodeToString(valueToEncode.getBytes());
    }
}
