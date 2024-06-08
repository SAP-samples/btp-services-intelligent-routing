package com.sap.region.manager.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class Config {
    private boolean performanceTraceEnabled;
    private List<Pair> regionNames; 
}
