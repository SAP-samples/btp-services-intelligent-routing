# Introduction

In this step, you will create a REST API in SAP Cloud Integration that will act as an auxiliary endpoint for GCP Uptime checks. 

**Important:** The following steps need to executed for both SAP Cloud Integration tenants. 
## Setup Monitoring Endpoint for GCP updtime checks

1. Go to the SAP Cloud Integration web interface of your first subaccount, either via the SAP Integration Suite launchpad as shown in [Setting SAP Cloud Integration](../01-SetupCloudIntegration/README.md). 

2. <a name="download"></a>Open a new tab in your browser and go to https://github.com/SAP-samples/btp-services-intelligent-routing/blob/ci_gcp/02-SetupMonitoringEndpoint/Multi-Region-HA.zip. **Download** the ZIP File containing the Integration Package with the sample REST API for SAP Cloud Integration.

    ![Download Button on GitHub](./images/01.png)

3. Go back to the SAP Cloud Integration web interface and choosse **Design** in the navigation area. 

    ![Navigate to the Design menu](./images/02.png)

    > Note: You can expand the navigation area using the hamburger icon. 

4. Import the previously downloaded Integration Package (ZIP file from Step 2).  

    ![Import content package](./images/03.png)

5. Select the newly uploaded Integration Package called **Multi-Region HA**.

    ![Select new integration package](./images/04.png)

6. Go to the **Artifacts** tab and click on the **Health Check** artifact of type REST API to open the SAP Cloud Integration editor. 

    ![Select new REST API](./images/05.png)

    You should now see a very basic REST API that offers an HTTP endpoint and returns a message using the HTTP body to the sender. This REST API is used by GCP Health check to call this REST API in both SAP Cloud Integration tenants in order to find out if the tenant is up and running. 
    

7. Change into the **Edit** mode and confirm potential messages with **Yes**.

    ![changed into edit mode](./images/06.png)

8. Double-Click **Reply Tenant Identifier**.

    ![changed into edit mode](./images/07.png)

9. Select the **Message Body** tab so you can type in whatever message you want to reply to the sender. For testing purposes it helps, if you replace the placeholder with the subaccount region, the current SAP Cloud Integration instance is located in. That way, you can easily identify which tenant is handling the traffic routed by Google Cloud DNS. 

    ![replace placeholder Message Body](./images/08.png)

10. **Save** and **Deploy** the REST API. 

    ![Save and deploy](./images/09.png)

11. Open a new browser tab and navigate to your subaccount in the SAP BTP Cockpit. 

12. Create space in Cloud Foundry environment to control use of resources. Therefore, **Enable Cloud Foundry**. 

    ![Enable Cloud Foundry button](./images/10.png)

    > If you already have a Cloud Foundry space, you can skip steps 12 - 15. 

13. Provide unique names for the instance and org name. 

14. Create a new Cloud Foundry space using the **Create Space** button.

    ![Create space button](./images/11.png)

15. Enter a space name and select the permissions you would like to assign to your ID and save the changes by using the **Create** button.
    
    ![space name and creation button](./images/12.png)

16. Select the newly created space. 

    ![space name and creation button](./images/12.1.png)
    
17. Go to **Services > Service Marketplace** and search for **Process Integration Runtime**.

    ![Process Integration Runtime tile](./images/13.png)

18. Select the tile and **Create** a instance of the Process Integration Runtime service.

    ![Service instance creation](./images/14.png)

19. In the New Instance or Subscription dialog box, select the Plan **integration-flow** and provide a instance name. Continue with **Next**.

    ![Service instance name and plan](./images/15.png)

20. In the Parameter menu switch to the **JSON** tab and insert the following content in the text area to assign roles. This authorizes the sender system to call a tenant and allows to process the messages on the tenant.

    ```json
    {
        "roles": [
            "ESBMessaging.send"
        ]
    }
    ```

    > Note: The role name is case-sensitive and the authentication mode used is basic authentication.

    ![Service instance name and plan](./images/16.png)

21. Continue with **Create**. 

22. Select **View Instance** to see the progress of the service instance creation. 

    ![Service instance creation progress](./images/17.png)

23. <a name="servicekey"></a> In the Instance View, select **Create** in the **Service Keys** section to create a service key. 

    ![service key creation](./images/18.png)

24. Enter a name (e.g. sk1) for the service key and choose **Create**. 

25. Click on the recently created service key to retrieve the credential information from the service key. Keep this tab opened since you will need the credentials in one of the next steps.  

    ![service key detail](./images/19.png)

26. Go back to the tab with the SAP Cloud Integration web interface or open a new one. Navigate to the **Monitor** item in the navigation area. 

    ![SAP Cloud Integration Monitor section](./images/20.png)

27. In the **Manage Integration Content** area, select the **Started** tile to have a look at the REST API you have deployed in step 10. 

    ![SAP Cloud Integration Monitor section](./images/21.png)

28. <a name="endpoint"></a>Select **Health Check** and copy the endpoint URL of the REST API. 

    ![REST API endpoint](./images/22.png)

29. Open a terminal on your machine and try to execute the following command. 

    ```console
    curl <endpoint_from_sapcloudintegration> -u '<clientid_from_servicekey>:<clientsecret_from_servicekey>'
    ```

    - <endpoint_from_sapcloudintegration> is the endpoint URL you have just copied in the SAP Cloud Integration web interface. 
    - <clientid_from_servicekey> is the clientid value of the service key that you have created in step 25. 
    - <clientsecret_from_servicekey> is the clientsecret value of the service key that you have created in step 25.


    > The command could possibly look like: 
    > curl https://mysubaccount.it-cpi003-rt.cfapps.eu30.hana.ondemand.com/http/ha/health -u 'sb-50162a35-56d0-4c06-adb0-3f315df3b0c3!b2657|it-rt-xxxxyzwzwze!b196:af36f2ea-561a-44a3-977d-831f8ed9d129$Ta8rQN1LMzY9l9SvowftrpclBRqHNGJDvaX07veirIx='

    ![Hit REST API endpoint](./images/23.png)


    > Note: It could happen that Windows machines are trying to authenticate implicitly with certificates against the endpoint. This can cause problems. 
    > In case you're facing the error message "The filename, directory name, or volume label syntax is incorrect.", encode special characters like '|' in the username. 

30. **Repeat all the steps for the second subaccount.**

Congratulations! You have setup an endpoint in SAP Cloud Integration that will help Google Cloud DNS to identify whether one of the tenants is online or not. For sure, this REST API only offers a straight forward way to determine, whether the tenant is able to handle an incoming request or not. It's up to you to implement a more sophisticated logic to identify, if a tenant should be the one prioritized by Google Cloud DNS or not. 

    





