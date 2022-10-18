[![REUSE status](https://api.reuse.software/badge/github.com/SAP-samples/btp-services-intelligent-routing)](https://api.reuse.software/info/github.com/SAP-samples/btp-services-intelligent-routing)

# Route Multi-Region Traffic to SAP BTP Services Intelligently 

## Description

Critical business use cases built on SAP BTP need to be highly available and responsive. While availability zones help with disruptions in a single region, application developers and administrators are responsible for availability and stability in cases of regional outage or application upgrades. This scenario demonstrates techniques for load balancing, failover, and latency reduction of BTP services, such as SAP Launchpad and SAP Cloud Integration, with Hyperscaler traffic management solutions like Azure Traffic Manager.

### Challenge
A lack of automatic failover, especially for critical business scenarios, can be damaging to your business. If users are located in diverse geographies, latency will also increase if services are restored in a single region.

### Solution
Use your own domain for SAP BTP services, extensions, and integrations endpoints using the SAP Custom Domain Service. Configure Hyperscaler traffic management solutions such as Azure Traffic Manager and apply different profiles to decouple connection information.

### Outcome
A cloud-native integration pattern that incorporates SAP BTP and Hyperscaler services eliminates downtime, reduces global latency, and increases throughput.

### Implementations

#### [Intelligent Routing for SAP Cloud Integration using Azure Traffic Manager](https://github.com/SAP-samples/btp-services-intelligent-routing/tree/ci_azure)

In this scenario, you will learn how to achieve the high availability of a Cloud Integration flow built by a customer using SAP Custom Domain service & Azure traffic manager. 

#### [Multi-region High Availability for SAP Launchpad service on SAP BTP using Azure Traffic Manager](https://github.com/SAP-samples/btp-services-intelligent-routing/tree/launchpad_azure)

This scenario will be similar to the above one, but here we will achieve the high availability of the SAP Launchpad service, a SaaS application managed by SAP.

## How to obtain support
[Create an issue](https://github.com/SAP-samples/btp-services-intelligent-routing/issues) in this repository if you find a bug or have questions about the content.
 
For additional support, [ask a question in SAP Community](https://answers.sap.com/questions/ask.html).

## Contributing
If you wish to contribute code, offer fixes or improvements, please send a pull request. Due to legal reasons, contributors will be asked to accept a DCO when they create the first pull request to this project. This happens in an automated fashion during the submission process. SAP uses [the standard DCO text of the Linux Foundation](https://developercertificate.org/).

## License
Copyright (c) 2021 SAP SE or an SAP affiliate company. All rights reserved. This project is licensed under the Apache Software License, version 2.0 except as noted otherwise in the [LICENSE](LICENSES/Apache-2.0.txt) file.
