# Introduction

In this step, you will map the endpoints of the SAP Cloud Integration runtime to your custom domain using the SAP Custom Domain Service. Both of the SAP Cloud Integration runtime endpoints are then mapped to the same domain. 

This way, the sender to SAP Cloud Integration must not use the endpoint information of the particular SAP Cloud Integration tenants but uses the custom domain. 

The steps below describe the process with a domain bought via Azure. The process for a domain from another domain provider should be relatively similar. 

**Important:** The following steps need to executed for both of the subaccounts for SAP Cloud Integration 

## Map Custom Domain Routes

1. Install the Cloud Foundry CLI and make sure you have chosen the right API Endpoint (one of the subaccounts). Follow [this tutorial](https://developers.sap.com/tutorials/cp-cf-download-cli.html) in order to do so. 

2. Install the [custom domain plugin for the Cloud Foundry CLI](https://help.sap.com/viewer/65de2977205c403bbc107264b8eccf4b/Cloud/en-US/9f98dd0fcf9447019f233403f4ca60c1.html). 

3. Double check if you are logged in to the right environment:

    ```cmd
    cf target
    ```

4. Register your custom domain: 

    ```cmd
    cf create-domain primarydomain example.com
    ```

> Note: Instead of example.com use your own domain that you have bought. 

4. Check if the domain was registered: 

    ```cmd
    cf domains
    ```

5. Generate a new private and public key pair for this domain: 
   
    ```cmd
    cf custom-domain-create-key key1 "CN=*.example.com, O=Company, L=Cologne, C=DE" "*.example.com"
    ```

    ![Create signing request](./images/01.png)

6. Download certificate signing request corresponding to the new key:

    ```cmd
    cf custom-domain-get-csr key1 key1_unsigned.pem
    ```

    ![Download signing request](./images/02.png)

    > Note: The file key1_unsigned.pem is downloaded to your current working directory. Use `pwd` (for Linux/macOs) or `dir` (Windows) to find out the current working directory.

7. Install certbot client on local machine. 

   - for **Windows**: Download the latest version of the Certbot installer for Windows at https://dl.eff.org/certbot-beta-installer-win32.exe. Run the installer and follow the wizard. The installer will propose a default installation directory, C:\Program Files(x86)

    - for macOS: execute ```brew install certbot``` to install the certbot client. 

> for all others: Go to https://certbot.eff.org/lets-encrypt/osx-other and choose "My HTTP website is running on **none of the above** on **choose your OS**. 

8. Sign the certificate signing request (with a domain bought from Azure): 


    **Windows:**
    ```cmd
    certbot certonly --manual --preferred-challenges dns --server "https://acme-v02.api.letsencrypt.org/directory" --domain "*.example.com" --email your.mail@example.com --csr key1_unsigned.pem --no-bootstrap --agree-tos
    ```
    **macOS**
    ```cmd
    sudo certbot certonly --manual --preferred-challenges dns --server "https://acme-v02.api.letsencrypt.org/directory" --domain "*.example.com" --email your.mail@example.com --csr key1_unsigned.pem --no-bootstrap --agree-tos
    ```

    ![DNS Challenge 01](./images/03.png)

    > Don't forget to fill in your domain and mail address instead of example.com! You now have to proof that you are in control of the domain - certbot is now executing a DNS challenge. 

9. Open a new broswer tab, go to the [Azure Portal](http://portal.azure.com) and navigate into the DNS zone of your bought domain. 

    ![DNS Zone Search](./images/04.png)
    ![DNS Zone selection](./images/05.png)

10. **Create a new record set** and enter the details that the certbot command (Step 8) has printed out. 

    ![Record creation in DNS Zone](./images/06.png)

11. Hit **Enter** in the Terminal (where you have recently executed the certbot command in Step 8) to continue the verification process. 

    ![Verification process continuation ](./images/07.png)

    > IMPORTANT: sometimes it could happen that you have to repeat Step 10 & Step 11 a few times, depending on the output in the terminal. 

12. Open the certificate chain that has been created in the previous step in a text editor of your choice. 

    ![Certificate in text editor](./images/08.png)

13. Open a new broswer tab, go to <https://www.identrust.com/dst-root-ca-x3> and copy the content of the entire DST Root CA X3 Certificate. 

    > Don't forget to copy the entire content including '-----BEGIN CERTIFICATE-----' and '-----END CERTIFICATE-----'
    
14. Paste the content of the DST Root CA X3 Certificate to the end of the created certificate chain on your local machine that you have opened during step 12. Save it as a new file, for instance **certificate1.pem**. 

    ![Certificate in text editor](./images/09.png)

15. Upload and activate the certificates: 

    ```cmd
    cf custom-domain-upload-certificate-chain key1 certificate1.pem
    ```

    ![Custom domain upload certificate](./images/10.png)

    > Note: *key1* refers to the key you have created in Step 5, certificate.pem is the name of the file that you have created in the previous step.

16. Activate the custom domain: 

    ```cmd
    cf custom-domain-activate key1 "*.example.com"
    ```

    ![Custom domain activation](./images/11.png)


17. Verify the custom domain activation: 

    ```cmd
    cf custom-domain-list
    ```

    ![Custom domain activation verification](./images/12.png)

    > Note: It can take a few minutes up to a few hours until the custom domain is activated. 

18. <a name="endpointmapping">Finally, map the SAP Cloud Integration runtime endpoint to a subdomain of your domain: 

    ```cmd
    cf custom-domain-map-route <endpoint_from_sapcloudintegration> cloudintegration.example.com
    ```

    > Example: cf custom-domain-map-route https://mysubaccount.it-cpi003-rt.cfapps.eu20.hana.ondemand.com/http/ping cloudintegration.saptfe-demo.com. 

    > Note: If you don't know your SAP Cloud Integration runtime endpoint, go back to the [previous exercise, step 28](../02-SetupPolicyEndpoint/README.md#endpoint) - don't include a specific endpoint for an Integration flow or REST API, just the basic endpoint without any path. 



19. **Execute all of the steps for the second subaccount!**

Congratulations! You have created a certificate for your domain using Certbot. With the help of the SAP Custom Domain Service you registered the domain in the subaccounts in which you also provisioned SAP Cloud Integration and mapped its runtime endpoints to the actual domain.

Keep in mind that the Let's Encrypt certificate (the one you have create using Certbot) is only valid for three months. There are other more sophisticated alternatives for productive scenarios. 



    





