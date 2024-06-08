## Need for State & Event replication

###  1. State Replication using SAP HANA Cloud
SAP Cloud Integration offers a variety of integration patterns that store state within the same tenant when building integration scenarios. These patterns include variables, persistence, idempotency, NRE, and data stores. Additionally, there are several adapters that use implicit state management, such as the XI adapter, SAP SuccessFactors, MDI, and AS2.

While sufficient for single-region setups, these standard components pose limitations in multi-region configurations. Data remains confined to a single region, lacking replication capabilities across tenants or regions. SAP HANA Cloud serves as an external persistence database, offering explicit state management, but its replication capabilities are confined within the same availability zone or region, omitting cross-regional replication.

Leveraging SAP HANA SDA (Remote Table Replication) enables cross-regional data replication, supporting various functionalities such as data persistence, lookup, number range generation, and duplicate record handling. Integrating SAP HANA Cloud as an external persistence database ensures robust and efficient handling of data in integration scenarios.

However, with SAP HANA SDA, only one region's HANA DB can be write-enabled while others are read-enabled. In a multi-region architecture, switching between regions leads to write requests to the HANA DB failing. This requires manually steps to change the normal tables (write enabled) to virtual tables (read enabled) and virtual tables to normal tables.

### 2. Asynchronous Messaging
The SAP Cloud Integration's JMS adapter facilitates asynchronous communication patterns, allowing for the decoupling of message processing between the sender and receiver. This is particularly crucial for scenarios that require a high degree of reliability, such as long-running background processes or when implementing guaranteed delivery patterns. By ensuring that any necessary retries occur within the integration system rather than at the sender's end, the integrity and efficiency of the message delivery process are maintained. Additionally, in the event of downtime in the Advanced Event Mesh (AEM) service, the Disaster Recovery (DR) Replication bridge serves as a contingency mechanism. It ensures message continuity by replicating all messages from the affected region broker to a secondary region broker, thus providing an uninterrupted flow of communication and safeguarding against data loss.

This again needs a manual setup and when one region goes down the respective AEM broker should be changed to standby and the new active region AEM broker should be changed to Active.


## Solution Architecture
The solution diagram below illustrates a multi-region setup with SAP Advanced Event Mesh integration and SAP HANA Cloud for Cloud Integration Flows.

![architecture](../images/Azure%20CI%20HA%20-%20Reference%20Architecture.png)

In this architectural setup, SAP Cloud Integration and AEM are set up with Disaster Recovery Replciation bridge across two regions. The *Multi-Region Manager service* (MRM) is deployed across these regions to facilitate the replication of messages in the active region based on the availability of the region. Azure Traffic Manager is used for routing requests to different region subaccounts based on failover conditions.

### Need for the Multi-Region Manager (MRM) CAP application
The Advanced Event Mesh service lacks support for automatic switching between active and standby instances during regional downtime. Consequently, an external service is necessary to trigger the required steps automatically or manually in the event of a failover. Without this intervention, AEM messages may become stuck. To address this, the Multi-Region Manager (MRM) service steps in, deploying and undeploying Consumer iFlows as needed.

Additionally, SAP HANA DB does not inherently support automatic switching between normal tables (write-enabled) and virtual tables (read-enabled) during failovers. Here again, the Multi-Region Manager (MRM) plays a crucial role. It ensures seamless transitions between HANA DBs in different regions, updating read and write statuses as necessary. 

#### Initial Configuration and Setup overview
-   Azure Traffic Manager: Sends health check requests to each endpoint at regular intervals to verify that the SAP Cloud Integration is operational, ensuring that traffic is directed to the healthy region iFlows. Initially the primary region endpoint is US with priority 1 and secondary region endpoint is EU with priority 2.

- SAP Advanced Event Mesh (AEM) with Disaster Recovery (DR) replication bridge from Primary region (Active) to Secondary Region (Standby)

- SAP HANA DB Replication is setup from Primary Region (US, Write enabled) to Secondary Region (EU, Read enabled only).

- Producer iFlow which will be called externally and this will push the message to the AEM queue. Consumer iFlow will be triggered by the AEM asynchronously.

- MRM (Multi-Region Manager) has to be setup to be triggered by the manually by the admin or automatically using the Azure Traffic Manager alerts.

- (Optional for Automatic scenario) Two alerts are configured in the Azure traffic manager, alert-us and alert-eu. When the US region is down alert-us will trigger with status "Fired" and when US is back up again then the raised alert-us will be closed with "Resolved" status. This process is same for alert-eu and EU region.

- (Optional for Automatic Scenario) The configured alerts will trigger the Multi-Region Manager (MRM) application based on the configuration and alert status to update the Active and Standby regions in AEM. This wil also deploy & undeploy the consumer iFlows.

More details of the setup is shared in the End-to-End setup section.

#### Sequence diagram flow (Manual Scenario)

This sequence diagram illustrates the manual Multi-Region Manager (MRM) service flow where an admin will initiate the switch of the region.

![sequence](images/Stateful%20CI%20Sequence-Manual.png)

- Producer iFlow requests are routed to the primary region (US) as per the initial configration where the priority is set as 1 for US endpoint. This producer iFlow will then stores the message in the AEM queue and is also replicated to secondary region. AEM then pushes the message to the consumer iFlow asynchronously.

- Then the consumer iFlows may user external persistancy using SAP HANA DB, which replicates the data storted to the secondary region.

- In the manual setup, admin can now trigger the switch of the region from primary to secondary. This has to be done in a sequence of steps which will be explained further.

- In the first step, the admin will execute "Stop Operations", which disables the traffic manager profile. This stops all the incoming requets via the custom domain URL. Secondly, the AEM brokers (EU & US) are updated to "Standby" this will ensure that events will not trigger the iFlows.

- In the second step, both the AEM & HANA DB will be monitered for replications and once all the operations are completed, the next step can be executed.

- In the third step, the region can be now safely switched from Primary to the Secondary region, which involves a series of steps executed by the MRM automatically.

    - First the priorities of the traffic manager profiles are reversed. EU will be updated with priority 1 and US with the priority 2. This ensures that all the new requests will be going to the EU region producer iFlows.

    - US iFlows are undelpoyed to avoid duplicate execution if anyone tries to access them directly

    - The current SAP HANA DB replication from US to EU is dropped. Then converts the EU HANA DB tables from virtual to normal tables (write enabled).

    - EU iFlows are now deployed and are ready to accept the requests.

- In the fourth step, Azure Traffic Manager profile is activated, which will now accept the new requests. These requests will now go the secondary region (EU).

- Now incase the admin wants to switch back to the primary region, the above steps can be performed again.

#### Sequence diagram flow (Automatic Scenario)

This sequence diagram illustrates the automatic Multi-Region Manager (MRM) service flow where the Azure Traffic Manager automatially detects the failover and initiates the switch of the region.

![sequence](images/Stateful%20CI%20Sequence-Automatic.png)

- Producer iFlow requests are routed to the primary region (US) as per the initial configration where the priority is set as 1 for US endpoint. This producer iFlow will then stores the message in the AEM queue and is also replicated to secondary region. AEM then pushes the message to the consumer iFlow asynchronously.

- Then the consumer iFlows may user external persistancy using SAP HANA DB, which replicates the data storted to the secondary region.

- When Azure Traffic Manager detects a health check failure in the primary region (US), it triggers failover to the secondary region (EU). This failover process typically spans from 10 to 30 seconds. After this, all the producer iFlow requests will route to the secondary region (EU).

- Subsequently, Azure Traffic Manager also activates an alert (alert-us) that triggers the Multi-Region Manager (MRM) application running on SAP BTP to update the primary AEM instance (US) to standby mode and the secondary AEM instance (EU) to active mode. This process may require approximately 3-5 minutes to complete (Alert takes 3-5 mintues to trigger). This will also deploy the EU region consumer iFlows which is essential to avoid the AEM messages getting stuck.
    **Note:** During this period, the consumer iFlows in the EU will not be called until the EU AEM appliance is set as active.

- End user producer iFlow requests are routed to the secondary region (EU) following a failure in the primary region (US).

- When Azure Traffic Manager detects the recovery of the primary region (US) from a failure, it triggers the alert again (alert-us) with "Resolved" status, which will undeploy the US region iFlows.

- Now when the secondary region (EU) is down, then the Azure Traffic Manager will route the requests back to primary region (US).

- Subsequently, Azure Traffic Manager also activates an alert (alert-eu) that triggers the Multi-Region Manager (MRM) application running on SAP BTP to update the primary AEM instance (US) to active mode and the secondary AEM instance (EU) to standby mode. This process may require approximately 3-5 minutes to complete (Alert takes 3-5 mintues to trigger). This will also deploy the US region consumer iFlows which is essential to avoid the AEM messages getting stuck.

- End user producer iFlow requests are routed to the primary region (US) following a failure in the secondary region (EU).

- When Azure Traffic Manager detects the recovery of the primary region (EU) from a failure, it triggers the alert again (alert-eu) with "Resolved" status, which will undeploy the EU region iFlows.

- Now the whole process repeats again with the primary region going down.

