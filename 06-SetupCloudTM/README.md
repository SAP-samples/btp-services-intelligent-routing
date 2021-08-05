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

    ![Cloud Transport management: Service instance creation](./images/08.png)

14. Select **Standard** as plan (type **Subscription**) and continue with **Create**. 

    ![Cloud Transport management: Service instance creation details](./images/09.png)

15. 










