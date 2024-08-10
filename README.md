[![REUSE status](https://api.reuse.software/badge/github.com/SAP-samples/btp-services-intelligent-routing)](https://api.reuse.software/info/github.com/SAP-samples/btp-services-intelligent-routing)

# Route Multi-Region Traffic to SAP BTP Services Intelligently 

## Description

As the adoption of SAP BTP services grows and they become integral to essential business operations, customers are progressing through the platform maturity spectrum, incorporating SAP BTP services into their vital business scenarios. It is acknowledged that SAP BTP services come with built-in multi-AZ resiliency features.

Nevertheless, to enhance the robustness of their solutions, many customers have expressed interest in a multi-region setup of SAP BTP services and use cases. This approach involves geographic redundancy and the use of a load balancer to ensure that, should one region face downtime, the load balancer promptly identifies the problem and reroutes requests to a functioning region, thus maintaining uninterrupted business continuity.

### Challenge
A lack of automatic failover, especially for critical business scenarios, can be damaging to your business if the complete region is unavailable. If users are located in diverse geographies, latency will also increase if services are restored in a single region.

### Solution
Use your own domain for SAP BTP services, extensions, and integrations endpoints using the SAP Custom Domain Service. Configure Hyperscaler traffic management solutions such as Azure Traffic Manager and apply different profiles to decouple connection information. This allows routing of the traffic to the healthy region.

### Outcome
A cloud-native integration pattern that incorporates SAP BTP and Hyperscaler services eliminates downtime, reduces global latency, and increases throughput.

> **Note**: Our evaluations showcased in the below implementations offers an architectural viewpoint on Disaster Recovery strategies that incorporate certain SAP BTP services, alongside hyperscaler services like Azure Traffic Manager or AWS Route 53. This also constitutes one of the potential methodologies for overseeing stateful failovers. It's important to note that this evaluation, the proposed architecture, and the Proof of Concept (PoC) are not part of a standard support package or a production-grade solution. Additionally, they do not cover the assessment of specific customer needs or bespoke use cases.

### Implementations

The implementation and architecture archetypes presented leverage Microsoft Azure services to illustrate key concepts. It’s important to note that equivalent patterns can be realized using comparable offerings from other hyperscale cloud providers, such as Amazon Route 53.

#### [Stateless Scenario - Multi-Region High Availability for SAP Cloud Integration using Azure Traffic Manager](https://github.com/SAP-samples/btp-services-intelligent-routing/tree/ci_azure)

In this scenario, you will learn how to achieve the high availability of a Cloud Integration flow built by a customer using SAP Custom Domain service & Azure traffic manager. 

#### [Stateless Scenario - Multi-region High Availability for SAP Work Zone, standard edtion using Azure Traffic Manager](https://github.com/SAP-samples/btp-services-intelligent-routing/tree/launchpad_azure)

This scenario will be similar to the above one, but here we will achieve the high availability of the SAP Build Work Zone, standard service, a SaaS application managed by SAP.

#### [State & Event Replication Scenario - Multi-region High Availability for SAP Cloud Integration using SAP HANA Cloud, SAP Advanced Event Mesh & Azure Traffic Manager](https://github.com/SAP-samples/btp-services-intelligent-routing/tree/ci_stateful_azure)

In a stateful scenario, SAP Cloud Integration flows needs the data to be replicated across regions for it's own internal state storage or to store any documents. On the other hand, JMS queues, which are specific to a region can also be replaced with the SAP Advanced Event Mesh, making use of it's DR replication bridge.

## How to obtain support
[Create an issue](https://github.com/SAP-samples/btp-services-intelligent-routing/issues) in this repository if you find a bug or have questions about the content.
 
For additional support, [ask a question in SAP Community](https://answers.sap.com/questions/ask.html).

## Contributing
If you wish to contribute code, offer fixes or improvements, please send a pull request. Due to legal reasons, contributors will be asked to accept a DCO when they create the first pull request to this project. This happens in an automated fashion during the submission process. SAP uses [the standard DCO text of the Linux Foundation](https://developercertificate.org/).

## License
Copyright (c) 2024 SAP SE or an SAP affiliate company. All rights reserved. This project is licensed under the Apache Software License, version 2.0 except as noted otherwise in the [LICENSE](LICENSES/Apache-2.0.txt) file.
