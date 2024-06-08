package com.sap.region.manager;

import java.net.InetAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;

import org.json.JSONObject;
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AemNumberRangeTest {
    private Logger logger = LoggerFactory.getLogger(AemNumberRangeTest.class);
    private String host = "https://flow.intici.saptfe-demo.com";
    private String auth = "Basic xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx";

    public void sendRequest() throws Exception{
        while (true) {
            try {
                String url =  host+"/http/ha/aem/publishNumberRange";
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(new URI(url))
                        .timeout(Duration.ofSeconds(10))
                        .headers("Authorization", auth, "Content-Type", "text/plain")
                        .GET()
                        .build();
                
                
                HttpResponse<String> response = HttpClient.newHttpClient().send(request, BodyHandlers.ofString());
                InetAddress[] inetSocketAddresses = InetAddress.getAllByName("flow.intici.saptfe-demo.com");
                if (response.statusCode() == 200) {
                    // JSONObject jsonObject  = ((JSONObject)((JSONObject)((JSONObject) XML.toJSONObject(response.body())).get("ElementName")).get("StatementName_response"));
                    // jsonObject.put("IP", inetSocketAddresses[0].getHostAddress());
                    logger.info("Published" + "  IP Address: " + inetSocketAddresses[0].getHostAddress());
                    url =  host+"/http/ha/aem/getNumberRange";
                    request = HttpRequest.newBuilder()
                        .uri(new URI(url))
                        .timeout(Duration.ofSeconds(10))
                        .headers("Authorization", auth, "Content-Type", "text/plain")
                        .GET()
                        .build();
                        response = HttpClient.newHttpClient().send(request, BodyHandlers.ofString());
                        if (response.statusCode() == 200) {
                            JSONObject jsonObject  = ((JSONObject)((JSONObject)((JSONObject) XML.toJSONObject(response.body())).get("ROOT")).get("select_response"));
                            jsonObject.put("IP", inetSocketAddresses[0].getHostAddress());
                            logger.info(jsonObject.toString());
                        } else {
                            logger.error(response.body() + "  IP Address: " + inetSocketAddresses[0].getHostAddress());
                        }

                } else {
                    logger.error(response.body() + "  IP Address: " + inetSocketAddresses[0].getHostAddress());
                }
            } catch (Exception ex) {
                logger.error("sendRequest failed.", ex);
            }
            Thread.sleep(5000);
        }
    }
    
    public static void main(String[] args) throws Exception {
        AemNumberRangeTest numberRangeTest = new AemNumberRangeTest();
        numberRangeTest.sendRequest();
        
    }
}
