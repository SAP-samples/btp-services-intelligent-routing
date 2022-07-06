# Introduction
In this step, you will setup content federation for your SAP S/4HANA Fiori Apps.

**Important**: Federation will be setup with one SAP S/4HANA system, where the SAP Fiori apps will be federated to the two SAP Launchpad services.

>Prerequisites
> - To test content federation, you need an SAP S/4HANA system on release 2020 and have administrator access to it. This tutorial describes the configuration of an [SAP S/4 HANA Fully Activated Appliance 30-day trial system](https://www.sap.com/products/s4hana-erp/trial.html). You can find more details about the process to start your SAP S/4HANA trial in this [Quick Start Document](https://www.sap.com/documents/2019/04/4276422b-487d-0010-87a3-c30de2ffd8ff.html#page=1).
> 
>   Please keep in mind that the trial is only free for 30 days, so only request the system, when you are ready to run the tutorial.
> - [Download & install the cloud connector](https://tools.hana.ondemand.com/#cloud) & the [technical documentation](https://help.sap.com/viewer/cca91383641e40ffbe03bdc78f00f681/LATEST/en-US/57ae3d62f63440f7952e57bfcef948d3.html)
> - You can also follow the [tutorial here](https://developers.sap.com/tutorials/cp-launchpad-federation-prepares4hana.html) to do the content federation.

## Exposing SAP S/4HANA system to SAP BTP using SAP cloud connector
1.  Login to SAP Cloud Connector.

    ![Cloud Connector](./images/10.png)

2.  Add your SAP BTP subaccount to the SAP Cloud Connector by clicking **Add Subaccount**.

    ![new subaccount](./images/11.png)

3.  Copy the below details from the SAP BTP subaccount to add them to the SAP Cloud Connector.

    ![Subaccount details](./images/12.png)

4.  Search for the region and enter the text after 'https://api.cf.' that you copied for the **API Endpoint** from step 3 and select the shown region.

    ![CC Sub account - Region](./images/13.png)
    ![CC Sub account - Region](./images/14.png)
    
5.  Enter the subaccount that you copied from Step 3.

    ![CC Sub account - subaccount](./images/15.png)

6.  Provide your SAP BTP email address and password, then click **Save**.

    ![CC Sub account - Save](./images/16.png)

7.  Click **Cloud To On-Premise** to create the virtual mapping to your SAP S/4HANA system.

    ![Cloud to On-Premise](./images/17.png)

8.  Click the **Plus** button to add an entry to Access Control.
    
    ![Cloud to On-Premise](./images/18_1.png)

9.  Select **ABAP System** as the Back-end type.
    
    ![Cloud to On-Premise](./images/19.png)

10. Select **HTTPS** as the protocol and select **Next**.
    
    ![protocol](./images/20.png)

11. Provide the **Internal Host** & **Internal Port** and click **Next**.
    
    ![internal](./images/21.png)
    
12. Enter *s4hana* as **Virtual Host** & *44300* **Virtual Port** anc select **Next**.
    
    ![virtual](./images/22.png)

13. Select **None** and click **Next**.
    
    ![virtual](./images/23.png)
    
    >Note: In production scenarios, it is recommended to use Principal Propagation or better authentication methods. 

14. Click **Next**.
    
    ![ReqHead](./images/24.png)

15. Select the **Check Internal Host** checkbox and click **Finish**.
    
    ![Finish](./images/25.png)

16. You can see an entry in the **Access Control** tab. Now add resources by clicking **Add** button.
    
    ![Resources Add](./images/26.png)

17. Enter '/' in **URL Path** and select '*Paths and All Sub-Paths*' in **Access Policy**.

    ![URL Path](./images/27.png)

    >Note: In production systems, it is recommended to add the required URLs that needed to be exposed.

18. Click **Save**.

## Creating SAP S/4HANA design-time destinations in SAP BTP

The design-time destination is used to fetch the federated content from your SAP S/4HANA system during design-time.

19.  Navigate to **Connectivity > Destinations** in your SAP BTP subaccount and click **New Destination** button to create a new destination.
    ![Destination Navigate](./images/28.png)

20.  Fill in the following details.
    | Field Name | Value |
    |---|---|
    | Name  | s4hanadt  |
    | Type | HTTP|
    | Description | SAP S/4HANA design-time destination |
    | URL | http://s4hana:44300/sap/bc/ui2/cdm3/entities or http://yourvirtualhost:yourvirtualport/sap/bc/ui2/cdm3/entities |
    | Proxy Type | OnPremise |
    | Authentication | Basic Authentication |
    | User | *'User ID in your SAP S/4HANA system'* |
    | Password | *'Password of the user'* |

21.  Click **New Property** to add sap-client, e.g. *100* for SAP S/4HANA trial systems.
    ![Destination properties](./images/29.png)

22.  Click **Save**.

23. Repeat the steps from 19-22 in the second subaccount that you have configured for SAP Launchpad Service.

## Creating SAP S/4HANA runtime destinations in SAP BTP

The runtime destination is used to launch federated SAP S/4HANA applications at runtime.

24.  Create a new destination by cloning the destination **s4hanadt**.

     ![Destination Clone](./images/30.png)

25.  <a name="runtimedest"></a> Change the **Name** to *'s4hanart'* and change the **URL** to *'http://s4hana:44300'* or *'http://yourvirtualhost:yourvirtualport'*
    
     ![Properties](./images/31.png)

26.  Add additional properties as shown below:
    | Property Name | Value |
    |---|---|
    | HTML5.DynamicDestination  | true  |
    | sap-platform | ABAP |
    | sap-sysid | Your SAP S/4HANA system ID |

27.  Click **Save**.
    
     ![Save](./images/33.png)

28. Repeat the steps from 24-27 in the second subaccount that you have configured for SAP Launchpad Service.

## Configuring SAP S/4HANA system for Content Federation

29.  In your SAP S/4HANA system, open the transaction **spro**.
    
     ![spro tcode](./images/01.png)

30.  Click **SAP Reference IMG**.
    
     ![Reference IMG](./images/02.png)

31.  In the tree, navigate to path **SAP NetWeaver > UI Technologies > SAP Fiori > SAP Fiori Launchpad** and click **change Client-Specific Settings**.
    
     ![Clinet-Specific](./images/03.png)

32.  After opening the maintenance view, click **New Entries**.
    
     ![New Entries](./images/04.png)

33.  Enter the below information:

     | Field Name      | Value |
     | ----------- | ----------- |
     | FLP Property ID      | EXPOSURE_SYSTEM_ALIASES_MODE       |
     | Type   | String Type        |
     | Category | FLP UI Server Settings |
     | Property Value || CLEAR |

34.  Click **Save**, and if it prompts for the customizing request, select an existing project-related request or create a new one and click **Ok** icon.
     ![Save](./images/05.png)
     ![Customizing Request](./images/06.png)

35.  Activate **cdm3** service, if it is not activated yet. To do so, open the transaction **/nsicf** and enter *cdm3* in the service name and click **Execute**.
    
     ![sicf tcode](./images/07.png)
     ![cdm3 service](./images/08.png)

36.  Right-click on the service and click **Activate Service**.
    
     ![Activate service](./images/09.png)

37.  Enter the transaction code */n/ui2/cdm3_exp_scope*.
    
     ![Tcode](./images/34.png)

38. Click **Select Roles** to expose only the select roles for the content federation.
    
     ![Select Roles](./images/35.png)

39. Enter the role *SAP_BR_AP_ACCOUNTANT* and click **Save Selected Roles** icon.
    
     ![Select Roles](./images/36.png)

40. Click **Expose**.
    
     ![Select Roles](./images/37.png)
     
---

Congrats! You have successfully connected your SAP S/4HANA system with your SAP BTP subaccount using the SAP Cloud Connector. You have also created the necessary destinations to federate SAP Fiori apps from SAP S/4HANA, as well as exposed the necessary SAP S/4HANA roles.
