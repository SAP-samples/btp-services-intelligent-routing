# Introduction

In this step, you will configure Azure Traffic Manager (actually the Azure Traffic Manager profile). The Azure Traffic Manager profile is the key component in this *intelligent routing* scenario, as it defines which SAP Cloud Integration tenant should be used when based on certain rules and policies. 

This is the alternative step to "Configure Azure Traffic Manager using terraform" - you can also configure the Azure Traffic Manager profile using terraform instead of using the Azure Portal. 

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

8. <a name="tm-configuration">Provide the following settings: 

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

12. Open a new browser tab and navigate to the SAP BTP Cockpit, go to your first subaccount and open the **Instances and Subscriptions** view. Select the service with the service plan **integration-flow** and display the service key details. 

    ![Service Key details in SAP BTP Cockpit](./images/09.png)

    > You have created the service instance and service key in one of the previous exercises, [Setup Monitoring Endpoint](../02-SetupMonitoringEndpoint/README.md#servicekey).

13. Open another browser tab and go to <http://base64encode.org>. Encode the **clientid** and **clientsecret** from the service key (see previous step) in the following format: 

    ```
    <clientid>:<clientsecret>
    ```

    > Example: sb-50162a35-56d0-4c06-adb0-3f315df3b0c3!b2657|it-rt-xyz34567!b196:af36f2ea-561a-44a3-977d-831f8ed9d129$Ta8rQN1LMzY9l9SvowftrpclBRqHNGJDvaX07vxyz123

    ![Encoding clientid and clientsecret via base64encode.org](./images/10.png)


14. Copy the encoded information to your clipboard. 

15. Go back to the browser tab, in that you have started to define one of the endpoints for the Azure Traffic Manager profile. Provide the authorization information as **Custom Header Settings** in the following format: 

    ```
    Authorization:Basic <encoded clientid:clientsecret>
    ```

    > Example: Authorization:Basic c2ItNTAxNjJhMzUtNTZkMC00YzA2LWFkYjAtM2YzMTVkZjNiMGMzIWIyNjU3fGl0LXJ0LW1234WQ2ZWQydHJpYWwhYjE5NjphZjM2ZjJlYS01NjFhLTQ0YTMtOTc3ZC04MzFmOGVkOWQxMjkkVGE4clFOMUxNelk5bDlTdm93ZnRycGNsQlJxSE5HSkR2YVgwN3Zja1pt1234

    ![Add Authorization details as Custom Header Settings](./images/11.png)

    > Note: This way Azure Traffic Manager can authenticate against SAP Cloud Integration and can call the Integration Flow without getting an HTTP 403 response. Alternatively you can call the Integration Flow anonymously (so that the messages from Azure Traffic Manager to monitor the endpoints aren't counted as actual handled messages) but then you need to change the [monitoring settings](./README.md#tm-configuration) accordingly. A HTTP 403 response would then reveal Azure Traffic Manager that the endpoint is available, a 5xx HTTP response that the endpoint is not available. 

16. Continue with **Add**. 

17. You have created the first endpoint in the Azure Traffic Manager profile. **IMPORTANT: Repeat the Steps 10-16 for the other SAP Cloud Integration endpoint(s) in the other SAP BTP region(s).**

18. Display **Overview** of your Azure Traffic Manager profile and copy the **DNS Name**. 

    ![DNS Name of Azure Traffic Manager profile](./images/12.png)

19. Go to to the **DNS Zone** of your domain. 

    ![DNS Zone search using Azure Portal](./images/13.png)
    ![Select domain in Azure Portal](./images/14.png)

20. Create a record set for the subdomain that [you have mapped to the SAP Cloud Integration runtime endpoint](../03-MapCustomDomainRoutes/README.md#endpointmapping): 

    - Name: subdomain that you have mapped to the SAP Cloud Integration runtime endpoint
    - Type: CNAME
    - Alias Record Set: No
    - TTL: 1 Second (depending on your requirements, how fast a failover should be executed)
    - Alias: DNS Name of the Azure Traffic Manager profile that you have copied in Step 18 - without *http://*(e.g. cloudintegration-failover.trafficmanager.net)

    ![Select domain in Azure Portal](./images/15.png)

Congratulations. You have created an Azure Traffic Manager profile that detects which tenant should handle the messages based on a monitoring endpoint you have deployed (Integration Flow in both SAP Cloud Integration tenants) in one of the previous steps. All requests sent to the mapped route in Cloud Foundry (cloudintegration.example.com) are going to the Azure Traffic Manager profile because of the CNAME record set in the DNS Zone of the domain. Azure Traffic Manager then decides on the priority setting which tenant should handle the request. All of this happens on DNS level. 

A DNS Lookup shows the resolution: 

![DNS Lookup](./images/16.png)

> As an alternative to **dig** you can also use **nslookup** on Windows. 

You should also see that Azure Traffic Manager indicates that both SAP Cloud Integration as online right now. 

![DNS Lookup](./images/17.png)

