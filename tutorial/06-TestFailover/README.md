# Test the failover scenario

### You will learn
 - How to use different API endpoints from DB Switch - SAP CAP Application   

> ### Prerequisites
> - Make sure all the previous steps are completed and application end points are working.

### Sample iFlows for Testing
The following set of sample iFlows are designed to facilitate testing and exploration of external state management mechanisms.

[Multi-Region HA Sample](./iFlows/Multi-Region%20HA.zip)
[Multi-Region HA AEM Sample](./iFlows/Multi-Region%20HA%20AEM.zip)

 #### Number Ranges
In the context of iFlows, "number ranges" might be used to assign unique identifiers or sequence numbers to messages or transactions as they traverse through the integration flow. These numbers are important for tracking and managing the flow of data, especially in scenarios where multiple messages need to be processed in a sequential or ordered manner.

![Number Range](./images/iFlow_NRO.png)
In this sample iFlow, our aim is to confirm the incrementation of sequence numbers through external persistency, diverging from the default functionality of number ranges. 
#### Number Ranges with Asynchronous Messaging
The SAP Cloud Integration's JMS adapter facilitates asynchronous communication patterns, allowing for the decoupling of message processing between the sender and receiver.
###### Number Ranges with AEM Publisher  
![Number Range](./images/iFlow_NROAEMPublisher.png)

###### Number Ranges with AEM Consumer 
![Number Range](./images/iFlow_NROAEMConsumer.png)


### Testing External State Management

1. Import the [NRO](./iFlows/Multi-Region%20HA.zip) and [NRO AEM](./iFlows/Multi-Region%20HA%20AEM.zip) packages to both the regions.
2. Deploy all the imported package iFlows to active region. 
3. Create a SAP HANA Cloud Table and Stored procedure in both regions using the provided SQL statements.
    ```
        CREATE COLUMN TABLE "CI_TECH_USER"."NUMBER_RANGE" ("ID" BIGINT,
        "IFLOW_NAME" NVARCHAR(256),
        "MPL_ID" NVARCHAR(256),
        "CORRELATION_ID" NVARCHAR(256)) UNLOAD PRIORITY 5 AUTO MERGE;

        CREATE COLUMN TABLE "CI_TECH_USER"."AEM_NUMBER_RANGE" ("ID" BIGINT,
        "IFLOW_NAME" NVARCHAR(256),
        "MPL_ID" NVARCHAR(256),
        "CORRELATION_ID" NVARCHAR(256)) UNLOAD PRIORITY 5 AUTO MERGE;

        CREATE PROCEDURE "CREATE_NUMBER_RANGE"(IN IFLOW_NAME VARCHAR(256),IN MPL_ID VARCHAR(256),IN CORRELATION_ID VARCHAR(256),OUT LATEST_ID BIGINT)
        LANGUAGE SQLSCRIPT AS
        /*********BEGIN PROCEDURE SCRIPT ************/
        BEGIN
        DECLARE START_ID BIGINT;
        SELECT MAX (ID) into START_ID FROM "NUMBER_RANGE";
        IF START_ID IS NULL
        THEN START_ID := 1000000000;
        END IF;
        START_ID := START_ID + 1;
        INSERT INTO "NUMBER_RANGE" VALUES(:START_ID, IFLOW_NAME, MPL_ID, CORRELATION_ID);
        LATEST_ID := START_ID;
        END;

        CREATE PROCEDURE "CREATE_AEM_NUMBER_RANGE"(IN IFLOW_NAME VARCHAR(256),IN MPL_ID VARCHAR(256),IN CORRELATION_ID VARCHAR(256),OUT LATEST_ID BIGINT)
        LANGUAGE SQLSCRIPT AS
        /*********BEGIN PROCEDURE SCRIPT ************/
        BEGIN
        DECLARE START_ID BIGINT;
        SELECT MAX (ID) into START_ID FROM "AEM_NUMBER_RANGE";
        IF START_ID IS NULL
        THEN START_ID := 1000000000;
        END IF;
        START_ID := START_ID + 1;
        INSERT INTO "AEM_NUMBER_RANGE" VALUES(:START_ID, IFLOW_NAME, MPL_ID, CORRELATION_ID);
        LATEST_ID := START_ID;
        END;

    ```
3. Evaluate the integration flow in both the regions by issuing a HTTP Client request as outlined below.
    ```
    GET https://xxxx-demo.com/http/ha/getNumberRange
    Authorization: basic xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
    Content-Type: text/plain
    ```
4.  Establish the replication by accessing the Multi Region Manager(MRM) 
    -   Click Change replication
    -   Select the region
    -   Select the Subscription as Create
    -   Click Ok
    ![establish replication](./images/CreateReplication.png)
    This triggers the background job and creates a replication between the tables under the schema mentioned in the User Provided Service (ups.json)

#### Testing Manual Switchover

3. Run the sample test application to send continuous request to iFlows
A test Java client `src\multi-region-test` developed specifically for this task, continuously dispatching requests to the Azure Traffic Manager endpoint. Alternatively, you're welcome to utilize other tools like JMeter for sending continuous requests.
1. Adjust the URL and credentials within the Java client. 
    ![AutoIncrementHanaTest](./images/AutoIncrementHanaTest.png)
    >Note: Authentication should be provided in Base64 format.   
2. Compile and run the Java client using Visual Studio Code or your preferred Integrated Development Environment (IDE)
![AutoIncrementHanaTestRun](./images/AutoIncrementHanaTestRun.png)
3. This will send the continuous requests to iFlow endpoints    
    ![run](./images/test.gif)
4. Let's initiate the manual switchover testing process.
    -   Open the MRM application and navigate to the Manual Switchover section. Click on Stop Operations to suspend all external traffic and asynchronous message consumption. This will cause our test application to fail because the external traffic is disabled.
    ![Switch Stop](./images/Test_ManualSwitch.png)
   - Monitor the status of various services, including replication and running iFlows, by selecting the `Monitoring` option. Proceed to the next step only after all replications are complete, and there are no pending messages or running iFlows. 
    ![Switch Monitoring](./images/Test_ManualSwitch-Monitoring.png)
   - Click on the `Switch region` option to change the region from primary to standby. This action triggers a series of background steps to switch the region, which can be monitored in the `Region Change Activities` section
     ![Switch](./images/Test_ManualSwitch-Switch.png)
   - Wait for the switch operation to complete, then restart the external traffic by clicking the `Start operation` option. This action will allow external traffic to resume, and you should observe the test application starting to work again.
    - Now, we will proceed with the final step for this manual switchover activity by clicking on "Start Replication". This action triggers a background task for RTR subscription, that won't disrupt our running test application.
   - When you navigate to the `HOME`, you'll observe the alterations in the active region along with other pertinent details.
   ![Switch](./images/Test_ManualSwitch-Home.png)
5. You can alternatively access the SAP HANA Cloud tables to review the Number Range Sequence.









