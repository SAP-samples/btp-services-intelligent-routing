# Introduction

In this step, you will map the endpoints of the SAP Cloud Integration runtime to your custom domain using the SAP Custom Domain Service. Both of the SAP Cloud Integration runtime endpoints are then mapped to the same domain. 

This way, a sender connecting to SAP Cloud Integration must not use the region-specific endpoint information of the particular SAP Cloud Integration tenants but simply uses the custom domain. 

The steps below describe the process using a custom domain bought via Azure. The process for a domain coming from another domain provider should be relatively similar. 

**Important:** The following steps need to be executed for both of the subaccounts running your SAP Cloud Integration subscriptions 

## Setup the Custom Domain Service

1. Create a new **Subscription** of the **Custom Domain Service** in your Subaccount. Please select the **standard** plan if you see different service plans. 

    ![Create Subscription](./images/CDS_Subscription.png)

2. Before using the **Custom Domain Manager**, please assign the respective **Custom Domain Administrator** Role Collection to your current SAP BTP Subaccount user, which will be available after successful subscription. 

    ![Assign Role Collection](./images/CDS_RoleCollection.png)

3. Return to your **Instances and Subscriptions** menu and open the **Custom Domain Service** subscription. 

    ![Open Subscription](./images/CDS_OpenSubscription.png)


## Create a new Custom Domain

1. Start by **reserving** your **Custom Domain** in the respective SAP BTP Region (e.g., eu10). Do so by clicking on the **Add Reserved Domain** button.  

    ![Reserve Domain 01](./images/CDS_ReserveDomain01.png)

2. Provide your **Custom Domain** in the respective popup and click on **Add**.

    ![Reserve Domain 02](./images/CDS_ReserveDomain02.png)

3. Once the domain is **reserved** in this SAP BTP region, you will see it in the list as follows.

    ![Reserve Domain 03](./images/CDS_ReserveDomain03.png)

4. Switch to the **Custom Domains** tab and click on the **Create Custom Domain...** dropdown and select **for your Subaccount's SaaS Subscriptions**. 

    ![Custom Domain 01](./images/CDS_CustomDomain01.png)

5. Select the **Integration Suite** and click on **Next Step**. 

    ![Custom Domain 02](./images/CDS_CustomDomain02.png)

6. Select your **Reserved Domain** and click on **Next Step**.

    ![Custom Domain 03](./images/CDS_CustomDomain03.png)

7. Decide whether you want to use a subdomain of your reserved domain. In our scenario, we will use the **Reserved Domain** as domain and do not make use of a subdomain. Click on **Finish**

    ![Custom Domain 04](./images/CDS_CustomDomain04.png)

8. You should now see the **Custom Domain** in your list of Custom Domains in Status **not active** (as we did not activate a certificate yet).

    ![Custom Domain 05](./images/CDS_CustomDomain05.png)


## Request a new SSL certificate

1. Switch to the **Server Certificates** menu, select the **Create Server Certificate...** dropdown and select the **for your (wildcard) Custom Domains** option
   
    ![Certificate 01](./images/CDS_Certificate01.png)

2. Provide an **Alias**, change the default **Key Size** if required and and click on **Next Step**.

    ![Certificate 02](./images/CDS_Certificate02.png)

3. Click on **Next Step**. 

    ![Certificate 03](./images/CDS_Certificate03.png)

4. Select your **Custom Domain** and the **wildcard version** of your Custom Domain as **Subject Alternative Names**. 

    > **Hint** - In a production scenario requiring a paid SSL certificate, you might go for a different selection here and skip for example the **wildcard** SAN to get a cheaper SSL certificate for your purpose.  

    ![Certificate 04](./images/CDS_Certificate04.png)

5. Double-check the **CommonName** which should be your **Custom Domain** including a wildcard prefix and provide a valid **e-mail address** before clicking on **Finish**. Ideally, the e-mail address provided is a technical **inbox** and not a private/personal e-mail address. 

    ![Certificate 05](./images/CDS_Certificate05.png)

6. You will see, that a new Certificate Signing Request is being created for your Server Certificate. 

    ![Certificate 06](./images/CDS_Certificate06.png)


## Sign your new SSL certificate

1. Once the **Certificate Signing Request** has been successfully created, please click on the **Get Certificate Signing Request** button. 

    ![CSR 01](./images/CDS_SignCSR01.png)

2. Click on the **Copy** button within the popup window to copy your **Certificate Signing Request**. 

    ![CSR 02](./images/CDS_SignCSR02.png)

3. Store the **Certificate Signing Request** in a new **pem** file on your local device and name it **csr.pem**.

    ![CSR 03](./images/CDS_SignCSR03.png)

4.  Install certbot client on local machine. 

    - for **Windows**: Download the latest version of the Certbot installer for Windows at https://dl.eff.org/certbot-beta-installer-win32.exe. Run the installer and follow the wizard. The installer will propose a default installation directory, C:\Program Files(x86)

    - for macOS: execute ```brew install certbot``` to install the certbot client. 
    > for all others: Go to https://certbot.eff.org/instructions and choose "My HTTP website is running on **other** on **choose your OS**. 

    > **IMPORTANT**: The output of the certbot commands will look slightly different depending on your OS. Screenshots were taken with macOS. 

5.  Sign the certificate signing request (with a domain bought from Azure): 

    **Windows (console with administrative rights might be required):**
    ```console
    certbot certonly --manual --preferred-challenges dns --server "https://acme-v02.api.letsencrypt.org/directory" --domain "*.example.com" --email your.mail@example.com --csr csr.pem --no-bootstrap --agree-tos
    ```
    **macOS**
    ```console
    sudo certbot certonly --manual --preferred-challenges dns --server "https://acme-v02.api.letsencrypt.org/directory" --domain "*.example.com" --email your.mail@example.com --csr csr.pem --no-bootstrap --agree-tos
    ```

    ![DNS Challenge 01](./images/CDS_SignCSR04.png)

    ![DNS Challenge 02](./images/CDS_SignCSR05.png)

    > Don't forget to fill in your domain and mail address instead of example.com! You now have to proof that you are in control of the domain - certbot is now executing a DNS challenge. 

6.  Open a new broswer tab, go to the [Azure Portal](http://portal.azure.com) and navigate into the DNS zone of your bought domain. 

    ![DNS Zone Search](./images/CDS_SignCSR06.png)
    ![DNS Zone selection](./images/CDS_SignCSR07.png)

7.  **Create a new record set** and enter the details that the certbot command (Step 8) has printed out. 

    ![Record creation in DNS Zone](./images/CDS_SignCSR08.png)

8.  Hit **Enter** in the Terminal (where you have recently executed the certbot command in Step 11) to continue the verification process. 

    **macOS**
    
    ![Verification process continuation ](./images/CDS_SignCSR09.png)

    **Windows**

    ![Verification process continuation ](./images/CDS_SignCSR10.png)

    > IMPORTANT: sometimes it could happen that you have to repeat the last steps a few times, depending on the output in the terminal. 

9.  Open the certificate chain that has been created in the previous step in a text editor of your choice. 

    ![Certificate in text editor](./images/CDS_SignCSR11.png)

10. Open a new browser tab, go to <https://letsencrypt.org/certs/isrgrootx1.pem>, download the certificate and copy the content of the entire ISRG Root X1 Certificate. 

    > Don't forget to copy the entire content including '-----BEGIN CERTIFICATE-----' and '-----END CERTIFICATE-----'
    
11. Paste the content of the ISRG Root X1 Certificate to the end of the created certificate chain on your local machine that you have opened during step 9. Save it as a new file, for instance **certificate1.pem**. 

    ![Certificate in text editor](./images/CDS_SignCSR12.png)

12. Switch back to the **Custom Domain Manager** web user interface and click on **Upload Full Certificate Chain**. 

    ![Upload Certificate Chain](./images/CDS_SignCSR13.png)

13. Paste the content of your **certificate1.pem** file into the popup window and click on **Next Step**.

    ![Upload Certificate Chain](./images/CDS_SignCSR14.png)

14. Double-check the imported certificate details and click on **Next Step**.

    ![Upload Certificate Chain](./images/CDS_SignCSR15.png)

15. Finish the import by clicking on **Finish**. 

    ![Upload Certificate Chain](./images/CDS_SignCSR16.png)


## Activate the certificate

1. Make sure you selected the correct server certificate from the list of certificates and click on **Activate**.

    ![Activate Certificate](./images/CDS_ActivateCert01.png)

2. Select the available Subject Alternative Names and click on **Next Step**.

    ![Activate Certificate](./images/CDS_ActivateCert02.png)

3. Select an existing TLS configuration or just click on **Next Step** to create a new one.

    ![Activate Certificate](./images/CDS_ActivateCert03.png)

4. Click on **Finish** to finalize the process of certificate activation. 

    ![Activate Certificate](./images/CDS_ActivateCert04.png)

5. You will see the Status changes to **In Progress** while the certificate is being activated. 

    ![Activate Certificate](./images/CDS_ActivateCert05.png)

6. Wait until the Status has finished to **active**.

    ![Activate Certificate](./images/CDS_ActivateCert06.png)


## Create a custom route for Integration Suite


1. Switch to the **SaaS Routes** menu in your **Custom Domain Manager** and click on **Create Custom Route**. 

    ![SaaS Route Mapping](./images/CDS_RouteMapping01.png)

2. Keep the checkbox checked and click on **Next Step** in the popup. 
   
   ![SaaS Route Mapping](./images/CDS_RouteMapping02.png)
   
3. Select the **Integration Suite** subscription and click on **Next Step**.
   
   ![SaaS Route Mapping](./images/CDS_RouteMapping03.png)
   
4. Replace the provided **route** with your own **SAP Cloud Integration Runtime Endpoint** and click on **Next Step**.

    > Note: If you don't know your SAP Cloud Integration runtime endpoint, go back to the [previous exercise, step 28](../02-SetupPolicyEndpoint/README.md#endpoint) - don't include a specific endpoint for an Integration flow or REST API, just the basic endpoint without any path. 

    ![SaaS Route Mapping](./images/CDS_RouteMapping04.png)

5. Select your **Custom Domain** and click on **Next Step**. 

    ![SaaS Route Mapping](./images/CDS_RouteMapping05.png)

6. Provide a subdomain in the **Hostname** field such as **cloudintegration** being used in this sample scenario and click on **Finish** to finalize the setup. 
   
   ![SaaS Route Mapping](./images/CDS_RouteMapping06.png)


## **Execute all of the steps for the second subaccount!**

Congratulations! You have created a certificate for your domain using Certbot. With the help of the SAP Custom Domain Service you registered the domain in the subaccounts in which you also provisioned SAP Cloud Integration and mapped its runtime endpoints to the actual domain.

Keep in mind that the Let's Encrypt certificate (the one you have created using Certbot) is only valid for three months. There are other more sophisticated alternatives for productive scenarios. 



    





