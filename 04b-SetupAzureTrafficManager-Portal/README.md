# Introduction

In this step, you will configure Azure Traffic Manager (actually the Azure Traffic Manager profile). The Azure Traffic Manager profile is the key component in this "intelligent routing" scenario, as it defines which SAP Cloud Integration tenant should be used when based on certain rules and policies. 

This is the alternative step to "Configure Azure Traffic Manager using terraform" - you can also configure the Azure Traffic Manager profile using terraforml instead of using the Azure Portal. 

## Setup Azure Traffic Manager profile

1. Go to the [Azure Portal](http://portal.azure.com) and log in. 

2. Search for **Traffic Manager profile** and select the corresponding item.

    ![Azure Traffic Manager profile search using Azure Portal](./images/01.png)

3. **Create** a new Traffic Manager profile. 

    ![Azure Traffic Manager profile search using Azure Portal](./images/02.png)

4. Provide a meaningful name (e.g. *cloudintegration-failover*) for the Azure Traffic Manager profile, select **Priority** as the Routing method and assign it to one of your subscriptions. If necessary, create a new Resource Group. 

    ![Azure Traffic Manager profile creation details](./images/03.png)

5. Continue with **Create**. 

6. Wait until the deployment was succesfully finished. Select **Go to resource** to navigate to the details of the profile.

    > Alternatively you can also refresh the list of all Azure Traffic Manager profiles and select the recently created Traffic Manager profile.

    ![Traffic Manager profile details after deployment](./images/04.png)

7. Select **Configuration** in the navigation area. 

    ![Traffic Manager profile details after deployment](./images/05.png)

8. Provide the following settings: 

    - Routing method: Priority
    - DNS time to live (TTL): 1
    - Protocol HTTPS
    - Port: 443
    - Path: /http/ping
    - Expected Status Code Range: 200-200
    - Probing interval: 10
    - Tolerated number of failures: 1
    - Probe timeout: 5

    ![Traffic Manager profile configuration settings](./images/06.png)

    > **IMPORTANT**: Those settings enable the fatest failover that's possible based on DNS time to live & the **fast endpoint failover settings**. The more often the **monitor** endpoint (/http/ping) the higher the amount of messages the SAP Cloud Integration needs to handle. How often the monitor endpoint is called is defined by the combination of probe timeout and probing interval. Adjust the settings for your productive scenario depending on your needs. 

    > Note: The path you have defined is later on used to monitor every defined endpoint in the Azure Traffic Manager profile. The exact path is then concatenated with the endpoints target that we'll define in one of the subsequent steps. **/http/ping* is the path the Integration Flow that you have deployed in one of the [previous exercises](../02-SetupMonitoringEndpoint/README.md#endpoint).

9. Continue with **Save**.

10. Select **Endpoints** in the navigation area. 

    ![Traffic Manager profile configuration settings](./images/07.png)

11. **Add* a new endpoint and set the following parameters:

    - Type: external endpoint
    - Name: Cloud Integration EU
    - Fully-qualified domain name (FQDN) or IP: SAP Cloud Integration runtime endpoint EU20 (without any protocol)
    - Priority: 1

    > Note: The SAP Cloud Integration runtime endpoint is the FQDN of the deployed Integration flow (without */http/ping*) that you have also mapped in the [previous exercise](../03-MapCustomDomainRoutes/README.md#endpointmapping). 

    ![Cloud Integration EU](./images/08.png)

12. Open a new browser tab and navigate to the SAP BTP Cockpit, navigate to your first subaccount and open the serviceon your machine and retrieve the service key for the service with the service plan **integration-flow**. You have created the service instance and service key in one of the previous exercises, [Setup Monitoring Endpoint](../02-SetupMonitoringEndpoint/README.md#servicekey).







