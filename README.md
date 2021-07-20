# Intelligent Routing for SAP Cloud Integration using Azure Traffic Manager

This repository contains code samples and step by step instructions for the SAP Discovery Center mission [Intelligent Routing for SAP Cloud Integration using Azure Traffic Manager TODO](https://discovery-center.cloud.sap/)

## Description

Extending your S/4Hana business processes with BTP includes making sure, that you are ready to handle disaster recovery scenarios. When your SAP backend performs a failover, BTP workloads need to switch too. This can happen on configuration or on deployment level and involves adding an abstraction layer to be able to switch routing targets without the need to touch S/4Hana backend configuration. For a timely recovery of the service chain, we need to apply automation to the process.

Furthermore, the decoupling of the connection allows to scale the endpoints globally as close to the end-users as possible, minimizing latency, increasing up-time and performance. Different routing techniques like performance-based, availability-based, weighted, or geo-based are typically used.

In this mission you will learn how to run multiple SAP Cloud Integration tenants (Integration Suite) in parallel to apply the mechanisms mentioned above. Azure Traffic Manager will play a significant role in routing the traffic intelligently to different SAP Cloud Integration tenants.

### Current Position - What is the challenge?
- Automatic Failover for SAP Cloud Integration (anticipating disaster recovery of S/4Hana, or regular CPI maintenance)
- Reducing Latency for SAP Cloud Integration globally (US users accessing CPI in Europe for instance)
- Load balancing between SAP Cloud Integration tenants (increasing throughput of your tenant beyond scale-up capabilities)

### Destination - What is the outcome?
A cloud native integration pattern that incorporates BTP and S/4Hana to eliminate downtime, reduce global latency and increase throughput. The approach can be applied to other BTP services the same way.
### How You Get There - What is the solution?
Learn about SAP Cloud Integration, Custom Domain Service and Azure Traffic Manager.

## Requirements

## Download and Installation

## Known Issues

## How to obtain support

[Create an issue](https://github.com/SAP-samples/<repository-name>/issues) in this repository if you find a bug or have questions about the content.
 
For additional support, [ask a question in SAP Community](https://answers.sap.com/questions/ask.html).

## Contributing

## License
Copyright (c) 2021 SAP SE or an SAP affiliate company. All rights reserved. This project is licensed under the Apache Software License, version 2.0 except as noted otherwise in the [LICENSE](LICENSES/Apache-2.0.txt) file.
