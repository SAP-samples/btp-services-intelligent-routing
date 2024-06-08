
package com.sap.region.manager.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.sap.region.manager.service.JobService;

/**
 * <p>
 * JobController class.
 * </p>
 *
 * @author shanthakumar.krishnaswamy@sap.com
 */

@RestController
@RequestMapping("job")
public class JobController {
    @Autowired
    private JobService jobService;

    @GetMapping("switchRegion")
    public ResponseEntity<String> switchRegion(@RequestParam String region, @RequestParam Long priority, @RequestParam boolean monitor, @RequestParam String notes) {
        try {
            Long jobId = jobService.switchRegion(region, priority, monitor, notes);
            return new ResponseEntity<>("" + jobId, HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>("Failed to start the job [" + ex.getMessage() + "]", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("hanaRemoteSubscription")
    public ResponseEntity<String> hanaRemoteSubscription(@RequestParam String region, @RequestParam String action, @RequestParam String notes) {
        try {
            Long jobId = jobService.hanaRemoteSubscription(region, action,notes);
            return new ResponseEntity<>("" + jobId, HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>("Failed to start the job [" + ex.getMessage() + "]", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("ciArtifacts")
    public ResponseEntity<String> ciArtifacts(@RequestParam String region,@RequestParam String[] packageIds, @RequestParam String action, @RequestParam String notes) {
        try {
            Long jobId = jobService.ciArtifacts(region, packageIds, action, notes);
            return new ResponseEntity<>("" + jobId, HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>("Failed to start the job [" + ex.getMessage() + "]", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }    

    @GetMapping("tmRoutingPriority")
    public ResponseEntity<String> tmRoutingPriority(@RequestParam Long priority, @RequestParam String notes) {
        try {
            Long jobId = jobService.tmRoutingPriority(priority,notes);
            return new ResponseEntity<>("" + jobId, HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>("Failed to start the job [" + ex.getMessage() + "]", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("tmAllRoutingChange")
    public ResponseEntity<String> tmAllRoutingChange(@RequestParam String trafficStatus, @RequestParam String notes) {
        try {
            Long jobId = jobService.tmAllRoutingChange(trafficStatus,notes);
            return new ResponseEntity<>("" + jobId, HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>("Failed to start the job [" + ex.getMessage() + "]", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("tmRoutingChange")
    public ResponseEntity<String> tmRoutingChange(@RequestParam String region, String trafficStatus, @RequestParam String notes) {
        try {
            Long jobId = jobService.tmRoutingChange(region, trafficStatus,notes);
            return new ResponseEntity<>("" + jobId, HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>("Failed to start the job [" + ex.getMessage() + "]", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("aemReplicationRoleChange")
    public ResponseEntity<String> aemReplicationRoleChange(@RequestParam String region,@RequestParam String replicationRole, @RequestParam String notes) throws Exception {
        try {
            Long jobId = jobService.aemReplicationRoleChange(region,replicationRole,notes);
            return new ResponseEntity<>("" + jobId, HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>("Failed to start the job [" + ex.getMessage() + "]", HttpStatus.INTERNAL_SERVER_ERROR);
        }    
    }
    @GetMapping("aemAllStandby")
    public ResponseEntity<String> aemAllStandby( @RequestParam String notes) throws Exception {
        try {
            Long jobId = jobService.aemAllStandby(notes);
            return new ResponseEntity<>("" + jobId, HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>("Failed to start the job [" + ex.getMessage() + "]", HttpStatus.INTERNAL_SERVER_ERROR);
        }    
    }
    @GetMapping("stopAllOperations")
    public ResponseEntity<String> stopAllOperations(@RequestParam String notes) {
        try {
            Long jobId = jobService.stopAllOperations(notes);
            return new ResponseEntity<>("" + jobId, HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>("Failed to start the job [" + ex.getMessage() + "]", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("startAllOperations")
    public ResponseEntity<String> startAllOperations(@RequestParam String notes) {
        try {
            Long jobId = jobService.startAllOperations(notes);
            return new ResponseEntity<>("" + jobId, HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>("Failed to start the job [" + ex.getMessage() + "]", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("replicationMonitor")
    public ResponseEntity<String> replicationMonitor(@RequestParam String region) {
        try {
            Long jobId = jobService.replicationMonitor(region);
            return new ResponseEntity<>("" + jobId, HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>("Failed to monitor [" + ex.getMessage() + "]", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("stopJob")
    public ResponseEntity<String> stopJob(@RequestParam Long jobId) throws Exception {
        try {
            if(jobService.stopJob(jobId)) {
                return new ResponseEntity<>("" + jobId, HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Failed to stop the job: " + jobId, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception ex) {
            return new ResponseEntity<>("Failed to stop the job [" + ex.getMessage() + "]", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("restartJob")
    public ResponseEntity<String> restartJob(@RequestParam Long jobId) throws Exception {
        try {
            Long restartJobId = jobService.restartJob(jobId);
            return new ResponseEntity<>("" + restartJobId, HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>("Failed to restart the job [" + ex.getMessage() + "]", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
    }

}
