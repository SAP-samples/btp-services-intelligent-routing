package com.sap.region.manager.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class Monitoring {
    private String serviceKey;
    private String serviceName;
    private String activeRegionKey;
    private String activeRegionName;
    private String standByRegionKey;
    private String standByRegionName;
    private String comments;   
    private String status;   
}
