# Setup SAP Advanced Event Mesh with Dynamic Messaging Routing (DMR) cluster

### You will learn
 - How to set up SAP Advanced Event Mesh 
 - Initial setup for Establishing the Dynamic Messaging Routing (DMR) cluster

> ### Prerequisites
> - Enterprise services (broker version 10.0 or later).

Note: Standard 100 services are not supported.
## Create SAP Advanced Event Mesh 
To create your first instance of `SAP Advanced Event Mesh`, you need to follow these steps:

1. Go to your SAP BTP cockpit global account and subaccount.
2. Choose Service Marketplace.
3. Subscribe to the application plan.
4. In Service Marketplace, search for "event mesh".
    - Select the tile for SAP Integration Suite, advanced event mesh.
    - Add the required broker service plans in the sub-account entitlements.
    - Under Application Plans, find the Standard application plan, choose the options menu on the right side of the row, and then choose Create.
    - In the New Instance or Subscription dialog box, leave the prepopulated Service and Plan settings and choose Next.
    Add the email address for the user who is the subaccount administrator and then choose Next.
    - Review the details and choose Create.

##  Creationg Dynamic Messaging Routing (DMR) Cluster
Please refer to the [document](./AEM-Replication-For-Disaster-Recovery_CA.pdf) for detailed steps