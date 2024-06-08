package com.sap.region.manager.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.sap.region.manager.model.Config;
import com.sap.region.manager.model.Monitoring;
import com.sap.region.manager.model.Pair;
import com.sap.region.manager.service.ApiService;
import com.sap.region.manager.service.MonitoringService;

/**
 * <p>ApiController class.</p>
 *
 * @author shanthakumar.krishnaswamy@sap.com
 */
@RestController
@RequestMapping("api")
public class ApiController {
    @Autowired
    private ApiService apiService; 
    
    @Autowired
    private MonitoringService monitoringService;
    
    @RequestMapping(value = "/config", method = RequestMethod.GET)
    public Config getConfig() {
        return apiService.getConfig();
    } 
    @RequestMapping(value = "/monitoring", method = RequestMethod.GET)
    public List<Monitoring> monitoring() {
        return monitoringService.getServiceStatus();
    } 

    @RequestMapping(value = "/ciProcessingArtifacts", method = RequestMethod.GET)
    public ResponseEntity<String> ciProcessingStatus(@RequestParam String region) {
        String monitor = apiService.getCIProcessingArtifacts(region);
        return new ResponseEntity<>(monitor, HttpStatus.OK);
    }  
    @RequestMapping(value = "/aemReplicationStatus", method = RequestMethod.GET)
    public ResponseEntity<String> aemReplicationStatus(@RequestParam String region) {
        String monitor = apiService.getAemReplicationQueueStatus(region);
        return new ResponseEntity<>(monitor, HttpStatus.OK);
    }
    @RequestMapping(value = "/hanaReplicationStatus", method = RequestMethod.GET)
    public ResponseEntity<String> hanaReplicationStatus(@RequestParam String region, @RequestParam int rowSize, @RequestParam int offset) {
        String monitor = apiService.getHanaReplicationStatus(region, rowSize, offset);
        return new ResponseEntity<>(monitor, HttpStatus.OK);
    }  
    
}   
