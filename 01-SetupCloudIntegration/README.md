# Introduction

In this step, you will subscribe to SAP Integration Suite and and configure SAP Cloud Integration. 

## Setup SAP Cloud Integration 

1. If you haven't used SAP Business Technology Platform or SAP Cloud Integration, please go to the corresponding SAP Discovery Center Mission. [TODO LINK einfügen zu dieser Mission mit Basic Phase](google.com)

2. Go to your SAP BTP Cockpit and make sure that you have two subaccounts in your global account. In this case, we have both of them on Azure - one in West Europe (Netherlands, EU20), one in West US (WA, US20). If you don't have them yet, click on **New Subaccount** in order to create a new subaccount. 

    ![New subaccount](./images/01.png)

> NOTE: If you have two subaccounts in regions where the [SAP Integration Suite is available](https://discovery-center.cloud.sap/serviceCatalog/integration-suite?region=all&tab=service_plan), you don't necessarily need to create separate accounts for this. You can simply reuse the existing ones, if you want.

3. Provide the necessary details for the new subaccount. 

   - Provide a subaccount name. 
   - Optional: Provide a description. 
   - Select Provider **Azure**. 
   - Select Europe (Netherlands) or another region, where SAP Integration Suite is available. The [SAP Discovery Center](https://discovery-center.cloud.sap/serviceCatalog/integration-suite?region=all&tab=service_plan) shows the available regions.  
   - Enter a Subdomain for your subaccount. This subdomain becomes part of the URL for accessing applications that you subscribe to from this subaccount.
   - Optional: If your subaccount is to be used for production purposes, select the Use for production option.

    ![Subaccount details](./images/02.png)

4. Save your changes. 

**A new tile appears in the global account page with the subaccount details.**

5. Make sure that you have two subaccounts in a region where the SAP Integration Suite is available. If you don't have two subaccounts for SAP Integration Suite yet, create another subaccount as explained in Step 3-4. 

5. In the navigation area of the global account, choose **Entitlements > Entity Assignments** and select the subaccounts in which you want to set up SAP Integration Suite. Continue with **Go**. 

    ![Entity Assignment filter](./images/03.png)  

6. Go to **Configure Entitlements** followed by **Add Service Plans** for the first subaccount. 

    ![Configure Entitlements](./images/04.png)  
    ![Add Service Plans](./images/05.png)  

7. Add the following entitlements: 

    - SAP Integration Suite (Service Plan: standard_edition or digital_edition or premium_edition)
    - Process Integration runtime (Service Plan: integration-flow)
    - Custom Domain Service (Service Plan: custom_domains)

8.  **Save** the changes. 

    ![Save Entitlement Assignments](./images/06.png)  



9.  Repeat steps 8-10 for the second subaccount. 



10. Go to **Subaccounts** and navigate to the first subaccount for the SAP Integration Suite. 

11. In the navigation area of the subaccount, choose **Services > Service Marketplace**.
   
12. Search for **Integration Suite** and choose **Create** in the overview page.
    
13. In the **New Instance or Subscription** dialog box, select the **Plan** and wait for the subscription to complete successfully.
    
14. Check the status of the submission in subscriptions section on the **Instances and Subscriptions** page. If the subscription is successful you'll notice the status of the Integration Suite shown as **Subscribed**.

15. In the navigation area of the subaccount, choose **Security > Role Collections** and search for **Integration_Provisioner**. 

16. Select the Role Collection and click **Edit**. 
    
17. Enter the mail address for your SAP BTP user and **Save** your changes. 

18. Go back to the **Instances and Subscriptions** page. Select the **Integration Suite** Subscription and choose **Go to Application** to launch the Integration Suite Launchpad. 

    >The Integration Suite Launchpad is a common launchpad for provisioning and onboarding users to the Integration Suite capabilities. The provisioned users can access the activated capabilities and explore the resources needed for using the Integration Suite service. All the Integration Suite capabilities are represented as tiles on the launchpad.

    >Note: In case if you are unable to view the Integration Suite Launchpad, see 2953114 Information published on SAP site.

19. In the Integration Suite launchpad, under **Capabilities** section, choose **Manage Capabilities**. The Provisioning application is launched.

    > Note: The Manage Capabilities action is available only to users with Integration_Provisioner role.

20.  In the Provisioning application, choose **Add Capabilities**. 


21. Select the checkbox for **Design, Develop and Operate Integration Scenarios** (SAP Cloud Integration). Continue with **Next** and **Activate** without any further modifications. 
    
22. After activating the required capabilitiy, navigate to the Integration Suite Launchpad by choosing the Integration Suite button on the header.
    
The activated capabilities appear as tiles under the Capabilities section.

At the top-right corner of each capability tile, you can choose the icon to bring out options that let you manage and view additional details of a capability.

> Note: This option is available only to users with Integration_Provisioner role.

23.  Navigate back to the global account in the SAP BTP Cockpit. 

24. Select **Boosters** in the navigation area of the page. 


    





