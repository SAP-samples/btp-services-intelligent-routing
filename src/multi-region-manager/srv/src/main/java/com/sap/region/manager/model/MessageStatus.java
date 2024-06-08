package com.sap.region.manager.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @AllArgsConstructor
public class MessageStatus {
    String subscriptionName;
    long receivedMessageCount;
    long appliedMessageCount;
}
