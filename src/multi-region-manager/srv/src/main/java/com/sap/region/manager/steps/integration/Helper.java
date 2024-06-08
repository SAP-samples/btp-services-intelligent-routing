/**
 * @author : Shanthakumar K
 * @mailto : shanthakumar.krishnaswamy@sap.com
 * @created : 07-04-2024, Sunday
 **/
package com.sap.region.manager.steps.integration;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * <p>Helper class.</p>
 *
 * @author shanthakumar.krishnaswamy@sap.com
 */
public class Helper {
    private static final Logger logger = LoggerFactory.getLogger(Helper.class);

    public static String getCsrfToken(Properties properties, HttpClient client) {
        String csrfToken = "";
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(properties.getProperty("url") + "/api/v1"))
                    .headers("Authorization", getBasicAuthenticationHeader(properties), "Accept",
                            "application/json", "X-CSRF-Token", "Fetch")
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
            csrfToken = response.headers().map().get("X-CSRF-Token").get(0);
        } catch (Exception ex) {
            logger.error("Failed to get X-CSRF-Token.", ex);
        }
        return csrfToken;
    }
    
    public static List<String> getDesigntimeArtifactIds(String packageId, Properties properties, HttpClient client) throws Exception{
        List<String> designtimeArtifactIds = new ArrayList<>();
        try {
            logger.info("Getting Designtime Artifact Ids");
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(properties.getProperty("url") + "/api/v1/IntegrationPackages('" + packageId
                            + "')/IntegrationDesigntimeArtifacts"))
                    .headers("Authorization", getBasicAuthenticationHeader(properties), "Accept", "application/json")
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                designtimeArtifactIds = getValues(response.body(), "Id");
                logger.info("Designtime Artifact Ids: "+ designtimeArtifactIds);
            } else {
                logger.error(response.body());
                throw new Exception(response.body());
            }
            
        } catch (Exception ex) {
            logger.error("Getting Designtime Artifact Ids failed.", ex);
            throw ex;
        }
        return designtimeArtifactIds;
    }

    public static boolean isArtifactDeployed(String artifactId, Properties properties, HttpClient client) throws Exception{
        boolean deployed = false;
        try {
            logger.info("Getting deployement status for artifact: "+artifactId);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(properties.getProperty("url") + "/api/v1/IntegrationRuntimeArtifacts('" + artifactId
                            + "')"))
                    .headers("Authorization", getBasicAuthenticationHeader(properties), "Accept", "application/json")
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                deployed = true;
            } 
        } catch (Exception ex) {
            logger.error("Getting deployment status failed.", ex);
            throw ex;
        }
        return deployed;
    }
    public static List<String> getValues(String jsonStr, String key) {
        JSONObject jsonObject = new JSONObject(jsonStr);
        JSONObject jsonObject1 = (JSONObject) jsonObject.get("d");
        JSONArray jsonArray = jsonObject1.getJSONArray("results");
        return IntStream.range(0, jsonArray.length())
                .mapToObj(index -> ((JSONObject) jsonArray.get(index)).optString(key))
                .collect(Collectors.toList());
    }
    public static final String getBasicAuthenticationHeader(Properties properties) {
        String valueToEncode = properties.getProperty("username") + ":" + properties.getProperty("password");
        return "Basic " + Base64.getEncoder().encodeToString(valueToEncode.getBytes());
    }

}
