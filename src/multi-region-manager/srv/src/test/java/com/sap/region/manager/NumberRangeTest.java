package com.sap.region.manager;

import java.net.InetAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import org.json.JSONObject;
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NumberRangeTest {
    private Logger logger = LoggerFactory.getLogger(NumberRangeTest.class);
    private String url = "https://flow.intici.saptfe-demo.com/http/ha/publishNumberRange";
    private String auth = "Basic XXXXXXXXXXXXXXXXXXXXXX";

    public void sendRequest() throws Exception{
        while (true) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(new URI(url))
                        .headers("Authorization", auth, "Content-Type", "text/plain")
                        .GET()
                        .build();
                HttpResponse<String> response = HttpClient.newHttpClient().send(request, BodyHandlers.ofString());
                InetAddress[] inetSocketAddresses = InetAddress.getAllByName("flow.intici.saptfe-demo.com");
                if (response.statusCode() == 200) {
                    JSONObject jsonObject  = ((JSONObject)((JSONObject)((JSONObject) XML.toJSONObject(response.body())).get("ElementName")).get("StatementName_response"));
                    jsonObject.put("IP", inetSocketAddresses[0].getHostAddress());
                    logger.info(jsonObject.toString());
                } else {
                    logger.error(response.body() + "  IP Address: " + inetSocketAddresses[0].getHostAddress());
                }
            } catch (Exception ex) {
                logger.error("sendRequest failed.", ex);
            }
            Thread.sleep(10000);
        }
    }
    
    public static void main(String[] args) throws Exception {
        NumberRangeTest numberRangeTest = new NumberRangeTest();
        numberRangeTest.sendRequest();
        
    }
}
