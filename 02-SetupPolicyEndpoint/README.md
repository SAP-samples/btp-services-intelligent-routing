# Introduction

In this step, you will create an Integration flow in SAP Cloud Integration that will act as an auxiliary endpoint for Azure Traffic Manager. 

## Setup Policy Endpoint for Azure Traffic Manager

1. Go to the SAP Cloud Integration web interface of your first subaccount, either via the SAP Integration Suite launchpad as shown in [Setting SAP Cloud Integration](../01-SetupCloudIntegration/README.md). 

2. Open a new tab in your browser and go to https://github.com/SAP-samples/btp-cloud-integration-intelligent-routing/blob/mission/02-SetupPolicyEndpoint/flow-azuretm.zip and **Download** the ZIP File containing the Integration Package with the sample Integration Flow for SAP Cloud Integration.

![Download Button on GitHub](./images/01.png)

3. Go back to the SAP Cloud Integration web interface and choosse **Design** in the navigation area. 

    ![Navigate to the Design menu](./images/02.png)

    > you can expand the navigation area using the hamburger icon. 

4. Import the previously downloaded Integration Package (ZIP file from Step 2).  

    ![Import content package](./images/03.png)

5. Select the newly uploaded Integration Package called **Azure Traffic Manager**.

    ![Select new integration package](./images/04.png)

6. Go to the **Artifacts** tab and click on the **ping** artifact of type REST API to open the Integration Flow editor. 

    ![Select new integration flow](./images/05.png)

    You should now see a very basic integration flow that offers an HTTP endpoint and returns a message using the HTTP body to the sender. This integration flow is used for different purposes: 

    - a) Azure Traffic Manager will call this Integration Flow in both SAP Cloud Integration tenants in order to find out if the tenant is up and running. 
    - b) A fictive sender will call this Integration Flow and will get back which tenant was chosen by Azure Traffic Manager. 

7. Change into the **Edit** mode. 

    ![changed into edit mode](./images/06.png)

8. Double-Click **Reply Tenant Identifier**.

    ![changed into edit mode](./images/07.png)

9. Select the **Message Body** tab so you can type in whatever message you want to reply to the sender. For testing purposes it helps, if you have replace the placeholder with the subaccount region the SAP Cloud Integration is located in. That way, you can easily identify which tenant is handling the traffic routed by Azure Traffic Manager. 

    ![replace placeholder Message Body](./images/08.png)

10. **Save** and **Deploy** the Integration Flow. 

    ![Save and deploy](./images/09.png)

11. Create Service Instance using the UI. 

12. Call the endpoint using the Terminal 

13. **Repeat the steps for the second subaccount.**

    





