# Architecting Multi-Region Resilience: An Approach for Stateful Failover of SAP Cloud Integration

## 1. Introduction

### 1.1 Background

As the reliance on SAP BTP services strengthens and their deployment in critical business operations grows, customers are progressing on the platform maturity spectrum and are increasingly incorporating SAP BTP services into essential business functions. It is recognized that any interruption in service continuity can have a profound effect on their operations, with the potential for substantial financial consequences in certain cases.

### 1.2 Customer Requirements
The SAP Integration Suite, like most SAP BTP services, achieves high availability (HA) by leveraging the multi-AZ (multiple Availability Zones) concept within a single region, which meets the needs of the majority of our customers. However, for those exceptional cases where customers require their SAP Cloud Integration tenant to fail over to another geographical region for disaster recovery (DR) or to meet specific compliance and regulatory requirements, a cross-region high availability and disaster recovery setup is essential to ensure adequate protection. This provision for regional failover also aligns with the business continuity objectives of such customers.

### 1.3 The existing constraints of SAP BTP regarding High Availability/Disaster Recovery (HA/DR) capabilities across multiple regions

At present, a multi-region HA/DR setup is not available as a standard, out-of-the-box platform capability. However, it can be implemented as a custom setup, with some known limitations.
- At present, SAP BTP services that incorporate persistence, such as the SAP Integration Suite and SAP HANA Cloud, do not feature cross-region replication capabilities. Additionally, there is no publicly available product roadmap that indicates the introduction of this functionality in the immediate future.
- Only one endpoint is used for the Health Check, meaning that if the Cloud Integration (iFlows) is operational, but another service such as SAP HANA Cloud is down, the user will not be switched to the failover setup. If this is insufficient, a more complex health check concept should be considered.
- Latency across globally distributed regions, including latency to SAP, should be considered.

### 1.4 Objectives

A way to handle the above customer requirements is by incorporating a multi-region architecture. Although [previous efforts](https://github.com/SAP-samples/btp-services-intelligent-routing/tree/ci_azure) have been made in this direction, a notable drawback arises with the potential loss of state in the event of a tenant crash or unavailability of a region as listed down in the limitations above. 

This repository is dedicated to exploring the various aspects of state replication, examining the different architectural approaches (active/passive, active/active) for addressing various customer requirements. Its goal is to provide a thorough understanding of the topic by delving into the complexities involved in replicating state across different scenarios. This includes investigating the technical components and solutions provided by SAP or third-party providers, as well as considering customer-driven approaches to achieve a multi-region high availability setup.

**DISCLAIMER:** Our assessment provides an architectural perspective on Disaster Recovery strategies using SAP HANA Cloud and SAP Advanced Event Mesh (AEM), in conjunction with hyperscaler services such as Azure Traffic Manager. It is one of the possible approaches for managing stateful failovers of SAP Cloud Integration. Please note that this assessment, architecture, and Proof of Concept (PoC) evaluation do not constitute a standard support offering or a production-ready solution, and they do not address the evaluation of specific customer requirements or custom use cases.

## 2. Multi-Region Architecture Design
### 2.1 Architecture Overview
The solution diagram below illustrates a hybrid and multi-cloud architecture design that integrates applications with SAP BTP services and solutions on various hyperscaler services. This approach allows for greater flexibility and scalability by leveraging the capabilities of multiple cloud providers.

![Architecture](images/Azure%20CI%20HA%20-%20Reference%20Architecture.png)
In this setup, a custom domain is used to create a common URL, rather than using the URL provided by the SAP BTP services/applications. The iFlows are deployed across multiple regions (subaccounts), and the hyperscaler load balancer configurations are leveraged to intelligently route requests from the custom domain URL to the healthy application based on health checks. In the event of a failover, the switch to the healthy application is seamless, as the URL accessed by the user remains the same. Behind the scenes, requests are routed to the healthy region based on the maintained configuration. This ensures that the system remains available and responsive, even in the event of a regional outage or other disruption.

What is different here from the stateful architecture is the usage of SAP HANA for persistancey and SAP AEM for messaging/event based scenarios. A custom application called MRM - Multi-Region manager is used to replicate the state of HANA and control the AEM events across two regions. 

### 2.2 Required services

Services required to implement this solution architecture are as follows: 
- 2 SAP Integration Suite (Cloud Integration) tenants 
- 2 SAP HANA Cloud Service instances
- 2 SAP Advanced Event Mesh (AEM) instances with 1 Message Brokers each, which are in [DMR Cluster](https://help.pubsub.em.services.cloud.sap/Features/DMR/DMR-Overview.htm)
- 1 HTTP/DNS based Load Balancer 

### 2.3 Architecture Deep Dive
Please refer to the detailed documentation provided in the link below for an in-depth exploration of the architecture and solution. It includes comprehensive explanations along with sequence diagrams to illustrate the flow of the solution.

[Architecture Deep Dive](./tutorial/README.md)


## 3. End-to-End Setup
Having delved into the concepts of state replication using HANA and Asynchronous messaging using SAP Advanced event mesh, here is the guide to understand complete setup required to realize this integration.

##### [Step 1: Setup Stateless multi-region Scenario](https://github.com/SAP-samples/btp-services-intelligent-routing/tree/ci_azure) is a prerequisite for the stateful setup

##### [Step 2: Setup SAP HANA Cloud and Establish the RTR](./tutorial/02-SetupHanaCloud/README.md)
##### [Step 3: Setup SAP Advanced Event Mesh with Dynamic Messaging Routing (DMR) Cluster](./tutorial/03-SetupAEM/README.md)
##### [Step 4: Setup Multi Region Manager service - CAP Application](./tutorial/04-SetupMRM/README.md)
##### [Step 5: Setup Azure Traffic Manager Alert rule (Optional)](./tutorial/05-SetupAlertRule/README.md) 
##### [Step 6: Testing the Failover](./tutorial/06-TestFailover/README.md)

## 4. Demo
#### Manual Switchover 

https://github.com/SAP-samples/btp-services-intelligent-routing/assets/1583418/bbab1d5a-2c7b-48fc-8b33-5132622942a8

## Performance

* The RTO for a manual switch is influenced by several factors and can take approximately 50 minutes for 500 iFlows. 
* Assuming near real-time replication and a maximum delay of 2 minutes for 500 tables, the RPO could be estimated around 2 minutes for the worst-case scenario

For further information, please refer to the detailed documentation available [here](./tutorial/07-Performance/README.md).

## Known Issues
While we successfully demonstrated the technical possibility of replicating state across two SAP Cloud Integration tenants in different regions, we identified several compromises and concerns during the assessment: 
* The need for development, upkeep, and support for non-standard components like the Multi-Region Manager (MRM), which will lead to higher TCO for the customer.
* Additional cost and maintenance efforts for Azure Traffic Manager, SAP HANA Cloud, SAP Advanced Event Mesh, and custom tools that the customer must develop and oversee for synchronizing security artifacts across both tenants.
* Additional investments and tooling for monitoring the multi-region setup, as well as the dedicated operations teams to handle switchover procedures.
* The requirement to reconstruct standard content and B2B iFlows (such as TPM) to comply with multi-region state persistence guidelines, demanding considerable refactoring and development work for the customer. 

## <a name="furtherreading"></a> Further Reading

GitHub: [High Availability of SAP Launchpad service](https://github.tools.sap/btp-use-case-factory/launchpad-ha)

GitHub: [Distributed Resiliency of SAP CAP applications](https://github.com/SAP-samples/cap-distributed-resiliency)

## How to obtain support

[Create an issue](https://github.com/SAP-samples/btp-services-intelligent-routing/issues) in this repository if you find a bug or have questions about the content.
 
For additional support, [ask a question in SAP Community](https://answers.sap.com/questions/ask.html).

## Contributing
If you wish to contribute code, offer fixes or improvements, please send a pull request. Due to legal reasons, contributors will be asked to accept a DCO when they create the first pull request to this project. This happens in an automated fashion during the submission process. SAP uses [the standard DCO text of the Linux Foundation](https://developercertificate.org/).

## License
Copyright (c) 2024 SAP SE or an SAP affiliate company. All rights reserved. This project is licensed under the Apache Software License, version 2.0 except as noted otherwise in the [LICENSE](LICENSES/Apache-2.0.txt) file.




