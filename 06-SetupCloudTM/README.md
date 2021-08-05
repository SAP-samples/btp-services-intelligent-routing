# Introduction

In this step you will setup SAP Cloud Transport Management for the SAP Cloud Integration tenants you are using in the Azure Traffic Manager profile. 

All tenants that have been specified in the Azure Traffic Manager profile must be identical in their content. Otherwise, inconsistent processing of the Integration Flows/REST APIs may occur across tenants. Therefore, the productive tenants must also be at the same stand. In the following, you will configure SAP Cloud Transport Management so that you automatically transport from one SAP Cloud Integration tenant in the development environment to both productive SAP Cloud Integration tenants. 

For the sake of simplicity we use the SAP Cloud Integration tenant of the SAP BTP Trial as the development (DEV) Tenant. You can also use any other SAP Cloud Integration tenant as your source system for SAP Cloud Transport Management. 

A potential landscape could look like this: 

![Landscape](./images/01.png)

## Setup SAP Cloud Transport Management

1. **(Optional)** If you don't have a third tenant available that acts as the development environment (from which you will transport to the other two productive tenants), [setup SAP Cloud Integration in the SAP BTP Trial](https://developers.sap.com/tutorials/cp-starter-isuite-onboard-subscribe.html). 

2. Open the SAP BTP Cockpit in the subaccount for the development (DEV) environment and look for **Content Agent** in the **Service Marketplace**. **Create** a service instance. 

    > all the following steps needs to be done in the DEV subaccount until you are asked to change the subaccount

    ![Content Agent in the Service Marketplace](./images/02.png)

3. Keep everything as it is and provide **contentagent** as the service instance name. Continue with **Create**.

    ![Content Agent: Service instance creation details](./images/03.png)

4. Select **View Instance** to open the details for the created service instance. 

5. **Create a Service Key**. 

    ![Content Agent: Service instance creation details](./images/04.png)

6. Provide **contentagent_key** as the **Service Key Name** and continue with **Create**. 

7. Search for **Process Integration Runtime** in the **Service Marketplace** and create an instance. 

8. Choose **api** as the service plan and **cloudintegration_api** as the service instance name. Continue with **Next**. 

    ![Process Integration runtime: Service instance creation details](./images/05.png)

9.  Provide the following **JSON** as service instance parameters and continue with **Create**. 

    ```json
    {
        "roles": ["WorkspacePackagesTransport"]
    }
    ```

    ![Process Integration runtime: Service instance creation JSON parameter](./images/06.png)

10. Select **View Instance** to open the details for the created service instance. 

11. **Create a Service Key**.

    ![Process Integration runtime: Service key creation](./images/07.png)

12. Provide **cloudintegration_apikey** as the **Service Key Name** and continue with **Create**. 

13. Search for **Cloud Transport Management** in the **Service Marketplace** and create an instance. 

    ![Cloud Transport management: Subscription creation](./images/08.png)

14. Select **Standard** as plan (type **Subscription**) and continue with **Create**. 

    ![Cloud Transport management: Subscription details](./images/09.png)

15. Additionally, Create a Service Instance of **Cloud Transport Management** for the Service Plan **standard**. 

    ![Cloud Transport management: Service instance creation](./images/10.png)

16. Make sure you are using the **standard** plan (type **Instance**) and provide **cloudtm** as the service instance name. 

    ![Cloud Transport management: Service instance creation details](./images/11.png)

17. Continue with **Create**. 

18. Select **Destinations** in the navigation area and create a **New Destination**. The Destination should be based on a **Service Instance** (select the tab) not on a blank template. 

    ![Content Assembly Destination creation](./images/12.png)
    
19. Select **contentagent** as the service instance and provide **ContentAssemblyService** (needs to be exactly this name, case-sensitive!). 

    > **Important**: Make sure the destination is called exactly **ContentAssemblyService** otherwise SAP Cloud Integration won't recognize this destination. 

    ![Content Assembly Destination creation detail](./images/13.png)

20. Continue with **Next**. 

    > the OAuth2 information from the service key of the selected service instance will now be loaded into the destination details. 

21. **Save** the Destination. 

    ![Saving the destination](./images/14.png)

22. Display the service key information of the **cloudintegration_api** service instance. 

    ![cloudintegration_api service details](./images/15.png)

23. Copy the values of **clientid**, **clientsecret** and **url** of the service key details. 

    ![cloudintegration_api URL](./images/16.png)

24. Open a new browser tab and create a **new Destination**. 

    > a new browser tab so you can later on simply copy&paste the values of service key (step 23)

    ![New Destination](./images/17.png)

25. Provide the following details: 

    Name: CloudIntegration
    Type: HTTP
    Description: Source Tenant SAP Cloud Integration
    URL: <url from step23>/api/1.0/transportmodule/Transport (e.g.: https://e2ed6ed2trial.it-cpitrial03.cfapps.ap21.hana.ondemand.com/api/1.0/transportmodule/Transport)
    Proxy Type: Internet
    Authentication: Oauth2ClientCredentials
    Token Service URL: <token url from step23> (e.g. https://e2ed6ed2trial.authentication.ap21.hana.ondemand.com/oauth/token)
    Client ID: <clientid from step23> (e.g. sb-12cf3456-7f95-4916-8c06-c9c43217e826!b2657|it!b196 )
    Client Secret: <clientsecret from step23> (e.g. c1234567-dd57-4980-8b7c-bb30d01f0c3f$dvquwICkH9Jic1crOw3qx08n9zbJFwvBRvfa0tmb8Sk=)

    ![New Destination details](./images/18.png)

26. **Save** the destination. 

27. Create a **new Destination**. 

28. Select **Service Instance** and select **cloudtm** (The Cloud Transport Management service instance), the name of destination does have to be **TransportManagementService**. 

    > **Important**: The destination name is canse-sensitive and needs to follow the naming convention. 

    ![SAP Cloud Transport Management Destination creation](./images/19.png)
















