# Introduction

In this step you will setup SAP Cloud Transport Management for the SAP Cloud Integration tenants you are using in the Azure Traffic Manager profile. 

All tenants that have been specified in the Azure Traffic Manager profile must be identical in their content. Otherwise, inconsistent processing of the Integration Flows/REST APIs may occur across tenants. Therefore, the productive tenants must also be at the same stand. In the following, you will configure SAP Cloud Transport Management so that you automatically transport from one SAP Cloud Integration tenant in the development environment to both productive SAP Cloud Integration tenants. 

For the sake of simplicity we use the SAP Cloud Integration tenant of the SAP BTP Trial as the development (DEV) Tenant. You can also use any other SAP Cloud Integration tenant as your source system for SAP Cloud Transport Management. 

A potential landscape could look like this: 

![Landscape](./images/01.png)

## Setup SAP Cloud Transport Management

1. **(Optional)** If you don't have a third tenant available that acts as the development environment (from which you will transport to the other two productive tenants), [setup SAP Cloud Integration in the SAP BTP Trial](https://developers.sap.com/tutorials/cp-starter-isuite-onboard-subscribe.html). 

2. Open the SAP BTP Cockpit in the subaccount for the development (DEV) environment and look for **Content Agent** in the **Service Marketplace**. **Create** a service instance. 

    > **IMPORTANT:** All the following artifacts are created in the subaccount of the development environment!

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

    - **Name**: CloudIntegration
    - **Type**: HTTP
    - **Description**: Source Tenant SAP Cloud Integration
    - **URL**: `<url from step23>/api/1.0/transportmodule/Transport`
        (e.g.: https://e2ed6ed2trial.it-cpitrial03.cfapps.ap21.hana.ondemand.com/api/1.0/transportmodule/Transport)
    - **Proxy Type**: Internet
    - **Authentication**: Oauth2ClientCredentials
    - **Token Service URL**: `<token url from step23>`
        (e.g. https://e2ed6ed2trial.authentication.ap21.hana.ondemand.com/oauth/token)
    - **Client ID**: `<clientid from step23>`
    - **Client Secret**: `<clientsecret from step23>`

    ![New Destination details](./images/18.png)

26. **Save** the destination. 

27. Create a **new Destination**. 

28. Select **Service Instance** and select **cloudtm** (The Cloud Transport Management service instance), the name of destination does have to be **TransportManagementService**. 

    > **Important**: The destination name is canse-sensitive and needs to follow the naming convention. 

    ![SAP Cloud Transport Management Destination creation](./images/19.png)

29. Continue with **Next** to fetch the service key information. 

30. Add a **New Property**: 

    sourceSystemId=DEV

    ![sourceSystemId as new property for the destination](./images/20.png)

31. Continue with **Save**. 

32. Create another Destination (still in the subaccount of the development environment): 

    - **Name**: CloudIntegration EU
    - **Type**: HTTP
    - **Description**: Cloud Integration EU - Production
    - **URL**: `https://deploy-service.cfapps.<region>.hana.ondemand.com/slprot/<OrgNameOfTarget>/<SpaceNameOfTarget>/slp`
      e.g.: https://deploy-service.cfapps.eu20.hana.ondemand.com/slprot/Org%20with%20Spaces_cloudintegration-eu/dev/slp
    - **ProxyType**: Internet
    - **Authentication**: BasicAuthentication
    - **User**: `<your username>`
    - **Password** `<your password>`

    **Replace <region> with the region of your  productive target subaccount, in this case it is eu20.**
    **Replace <OrgNameOfTarget> and <SpaceNameOfTarget> with the Cloud Foundry information of your productive target subaccount**. 

    **Important: If the URL contains spaces, because your Cloud Foundry org or space contains spaces, replace the spaces with the URL escape character %20**

    ![Create Destination for productive target subaccount](./images/21.png)

    > More information about Transport Destinations on [help.sap.com](https://help.sap.com/viewer/7f7160ec0d8546c6b3eab72fb5ad6fd8/Cloud/en-US/c9905c142cf14aea86fe2451434faed9.html)

33. **Save** the destination. 

34. Check the connection. You should get the following response:

    ![Check Connection for new Destination](./images/22.png)

35. Create another destination for the other productive target subaccount - similar to what you have done in step 32. 

    - **Name**: CloudIntegration US
    - **Type**: HTTP
    - **Description**: Cloud Integration US - Production
    - **URL**: `https://deploy-service.cfapps.<region>.hana.ondemand.com/slprot/<OrgNameOfTarget>/<SpaceNameOfTarget>/slp`
      e.g.: https://deploy-service.cfapps.us20.hana.ondemand.com/slprot/Org%20with%20Spaces_cloudintegration-us/dev/slp
    - **ProxyType**: Internet
    - **Authentication**: BasicAuthentication
    - **User**: `<your username>`
    - **Password** `<your password>`

    **Replace <region> with the region of your  productive target subaccount, in this case it is us20.**
    **Replace <OrgNameOfTarget> and <SpaceNameOfTarget> with the Cloud Foundry information of your productive target subaccount**. 

    **Important: If the URL contains spaces, because your Cloud Foundry org or space contains spaces, replace the spaces with the URL escape character %20**

    ![Check Connection for new Destination](./images/23.png)

36. Check the connection. You should get the following response:

    ![Check Connection for new Destination](./images/24.png)

37. Create a new Role Collection **CloudIntegrationTM** in the development subaccount to enable Transport Management in the SAP Cloud Integration development tenant: 

    ![Check Connection for new Destination](./images/25.png)

38. Select the created Role Collection from the list of all Role Collections. 

39. Select **Edit** and add the following roles: 

    - **AuthGroup_Admdinistrator**
    - **WorkspacePackagesTransport**

40. Assign the Role Collection to your user and **Save**. 

    ![Role Collection for SAP Cloud TM](./images/26.png)

41. Creat another Role Collection called **CloudTransportManagement**. 

42. Select the created Role Collection from the list of all Role Collections and click on **Edit**.

43. Add all roles of the application identifier beginning with **alm-ts**. 

    ![Role Collection for SAP Cloud TM](./images/27.png)

    > This is not a best practice to add all existing roles into a single Role Collection - this is only for the sake of simplicity in this exercise. 

44. Assign the Role Collection to your user and **Save**. 

    ![Role Collection for SAP Cloud TM](./images/28.png)

45. Open the web interface of SAP Cloud Integration the development environment and select **Settings** in the navigation area. 

46. Open the **Transport** Tab and select **Edit**. 

    ![Role Collection for SAP Cloud TM](./images/29.png)

47. Change the **Transport Mode** to **Transport Management Service**. 

48. **Save** the changes. 

49. Go to **Instances and Subscriptions** and open **Cloud Transport Management**. 

    ![Open Cloud Transport Management](./images/30.png)

50. Create the source Transport Node (development environment).

    - **Name:** DEV
    - **Description:** SAP Cloud Integration development Tenant
    - **Allow Upload to Node**: Yes

    Keep everything else as it is. 

    ![Open Cloud Transport Management](./images/31.png)

51. Continue with **OK**. 

52. Go back to the **Transport** configuration in SAP Cloud Integration and select **Check Configuration**. 

    > The Configuration checks if the CloudIntegration as well as the TransportManagementService destination is reachable. Additionally, if the sourceSystemId (property of the TransportManagementService destination) is defined as a Transport Node (*with Allow Upload to Node*) in SAP Cloud Transport Management. 

    ![Open Cloud Transport Management](./images/32.png)

53. Go back to **SAP Cloud Transport Management** and create a **Transport Route** for the primary productive SAP Cloud Integration tenant: 

    - **Name:** PROD_EU
    - **Description:** SAP Cloud Integration productive tenant - EU
    - **Forward Mode:**: Auto
    - **Content-Type:** Multi-Target Application
    - **Destination:** CloudIntegrationEU

    ![Open Cloud Transport Management](./images/33.png)

54. Create another **Transport Route** for the secondary productive SAP Cloud Integration tenant: 

    - **Name:** PROD_US
    - **Description:** SAP Cloud Integration productive tenant - US
    - **Forward Mode:**: Auto
    - **Content-Type:** Multi-Target Application
    - **Destination:** CloudIntegrationUS

    ![Open Cloud Transport Management](./images/34.png)























    


















