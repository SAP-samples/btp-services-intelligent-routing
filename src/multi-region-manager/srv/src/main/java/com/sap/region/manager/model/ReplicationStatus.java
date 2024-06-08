package com.sap.region.manager.model;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ReplicationStatus {
    private String region;
    private String sourceTable;
    private String replicationTable;
    private boolean replicationEnabled;
    private MessageStatus messageStatus;
}
