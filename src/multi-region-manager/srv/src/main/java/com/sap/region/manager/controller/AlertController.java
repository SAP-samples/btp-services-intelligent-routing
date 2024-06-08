package com.sap.region.manager.controller;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.sap.region.manager.service.JobService;

@RestController
@RequestMapping("alert")
public class AlertController {
    private Logger logger = LoggerFactory.getLogger(AlertController.class);
    @Autowired
    private JobService jobService;
    
    @Autowired
    @Qualifier("azureProperties")
    private Properties azureProperties;
    
    @PostMapping("processAlert")
    public ResponseEntity<String> processAlert(@RequestBody JsonNode payload) throws Exception {
        logger.debug(payload.toString());
        String monitorCondition = payload.get("data").get("essentials").get("monitorCondition").textValue();
        logger.info("MonitorCondition: "+ monitorCondition);
        String alertRule = payload.get("data").get("essentials").get("alertRule").textValue();
        logger.debug("AlertRule:"+ alertRule);
        Long jobId = null;
        if(alertRule.equalsIgnoreCase(azureProperties.getProperty("tm_alert_primary"))) {
            logger.info("Primary Region Handler Triggered");
            if(monitorCondition.equalsIgnoreCase("Fired")) {
                // Primary is Down
                // Update Primary Endpoint priority from 1 to 3
                // This will make the secondary HANA DB a normal table instead of the old replication table
                logger.info("Primary is Down, Update Primary Endpoint priority from 1 to 3");
                logger.info("Primary is Down, Removing the replication from Primary to Secondary in secondary");
                jobId = jobService.fired("primary", Long.valueOf(3));
            }else if(monitorCondition.equalsIgnoreCase("Resolved")){
                 // Primary is Up
                 // Starting the Replication from Secondary to Primary
                 logger.info("Primary is Up, Start Replication Subscription in secondary");
                 jobId = jobService.resolved("primary");
            }
        }else if(alertRule.equalsIgnoreCase(azureProperties.getProperty("tm_alert_secondary"))){
            logger.info("Secondary Region Handler Triggered");
            if(monitorCondition.equalsIgnoreCase("Fired")) {
                // Secondary is Down
                // Update Primary Endpoint priority from 3 to 1
                // Remove the replication from Primary to Secondary in Primary
                // This will make the primary HANA DB a normal table instead of the old replication table
                logger.info("Secondary is Down, Updating Primary Endpoint priority from 3 to 1");
                logger.info("Secondary is Down, Removing the replication from Primary to Secondary in Primary");
                jobId = jobService.fired("secondary", Long.valueOf(1));
            }else if(monitorCondition.equalsIgnoreCase("Resolved")){
                // Secondary is Up
                // Starting the Replication from Secondary to Primary
                logger.info("Secondary is Up, Starting Replication Subscription in primary");
                jobId = jobService.resolved("secondary");
            }
        }
        return new ResponseEntity<>("Background job is triggered with Job Id: "+jobId, HttpStatus.OK);
    }
}