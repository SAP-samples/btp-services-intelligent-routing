# Introduction

In this step, you will map the endpoints of the SAP Launchpad service runtime to your custom domain using the SAP Custom Domain service. Both of the SAP Launchpad service URL's are mapped to the same domain. 

This way, a user accessing the Launchpad service must not use the region-specific URL information of the particular Launchpad service tenant but simply uses the custom domain. 

The steps below describe the process using a custom domain bought via Azure. The process for a domain coming from another domain provider should be relatively similar. 

**Important:** The following steps need to be executed for both of the subaccounts running your SAP Launchpad service subscriptions 

## Map Custom Domain Routes

1. Install the Cloud Foundry CLI and make sure you have chosen the right API Endpoint (one of the subaccounts). Follow [this tutorial](https://developers.sap.com/tutorials/cp-cf-download-cli.html) in order to do so. 

2. Install the [custom domain plugin for the Cloud Foundry CLI](https://help.sap.com/viewer/65de2977205c403bbc107264b8eccf4b/Cloud/en-US/9f98dd0fcf9447019f233403f4ca60c1.html). 

3. Copy the **API Endpoint** and **Org Name** of your first subaccount to your clipboard. The information can be taken from the subaccount overview in the SAP BTP Cockpit. 
    ![API Endpoint](./images/00.png)

4. Log in to the right Cloud Foundry space that you have created in the previous exercises using the following command. Insert the corresponding **API Endpoint** from the previous step. 

    ```
    cf login -a <API_ENDPOINT>
    ```

5. Create an instance of the [Custom Domain Service](https://help.sap.com/viewer/product/CUSTOM_DOMAINS/Cloud/en-US) using the following command:

    ```
    cf create-service INFRA custom_domains customdomain
    ```

6. Register your custom domain: 

    ```console
    cf create-domain <cloudfoundry_org> example.com
    ```
    > Note: Instead of example.com use your own domain that you have bought. Replace the <cloudfoundry_org> with the **Org Name** you have copied from the subaccount overview in Step 3. 

7. Check if the domain was registered: 
    ```console
    cf domains
    ```

8. Generate a new private and public key pair for this domain: 
   
    ```console
    cf custom-domain-create-key keyazuretemp "CN=*.example.com, O=<Organization e.g. SAP>, L=<Location e.g. Walldorf>, C=<Country e.g. DE>" "*.example.com"
    ```

    ![Create signing request](./images/01.png)

9.  Download certificate signing request corresponding to the new key:

    ```console
    cf custom-domain-get-csr keyazuretemp keyazuretemp_unsigned.pem
    ```

    ![Download signing request](./images/02.png)

    > Note: The file keyazuretemp_unsigned.pem is downloaded to your current working directory. Use `pwd` (for Linux/macOs) or `dir` (Windows) to find out the current working directory.

10. Install certbot client on local machine. 

    - for **Windows**: Download the latest version of the Certbot installer for Windows at https://dl.eff.org/certbot-beta-installer-win32.exe. Run the installer and follow the wizard. The installer will propose a default installation directory, C:\Program Files(x86)

    - for macOS: execute ```brew install certbot``` to install the certbot client. 
    > for all others: Go to https://certbot.eff.org/instructions and choose "My HTTP website is running on **Other** on **choose your OS**.  

    > **IMPORTANT**: The output of the certbot commands will look slightly different depending on your OS. Screenshots were taken with macOS. 

11. Sign the certificate signing request (with a domain bought from Azure): 

    **Windows (console with administrative rights might be required):**
    ```console
    certbot certonly --manual --preferred-challenges dns --server "https://acme-v02.api.letsencrypt.org/directory" --domain "*.example.com" --email your.mail@example.com --csr keyazuretemp_unsigned.pem --no-bootstrap --agree-tos
    ```
    **macOS**
    ```console
    sudo certbot certonly --manual --preferred-challenges dns --server "https://acme-v02.api.letsencrypt.org/directory" --domain "*.example.com" --email your.mail@example.com --csr keyazuretemp_unsigned.pem --no-bootstrap --agree-tos
    ```

    ![DNS Challenge 01](./images/03.png)

    > Don't forget to fill in your domain and mail address instead of example.com! You now have to proof that you are in control of the domain - certbot is now executing a DNS challenge. 

12. Open a new browser tab, go to the [Azure Portal](http://portal.azure.com) and navigate into the DNS zone of your bought domain. 

    ![DNS Zone Search](./images/04.png)
    ![DNS Zone selection](./images/05.png)

13. **Create a new record set** and enter the details that the certbot command (Step 11) has printed out. 

    ![Record creation in DNS Zone](./images/06.png)

14. Hit **Enter** in the Terminal (where you have recently executed the certbot command in Step 11) to continue the verification process. 

    **Windows**

    ![Verification process continuation ](./images/07a.png)

    **macOS**
    
    ![Verification process continuation ](./images/07.png)

    > IMPORTANT: sometimes it could happen that you have to repeat the last steps a few times, depending on the output in the terminal. 

15. Open the certificate chain that has been created in the previous step in a text editor of your choice. 

    ![Certificate in text editor](./images/08.png)

16. Open a new broswer tab, go to <https://letsencrypt.org/certs/isrgrootx1.pem>, download the certficate and copy the content of the entire ISRG Root X1 Certificate. 

    > Don't forget to copy the entire content including '-----BEGIN CERTIFICATE-----' and '-----END CERTIFICATE-----'
    
17. Paste the content of the ISRG Root X1 Certificate to the end of the created certificate chain on your local machine that you have opened during step 15. Save it as a new file, for instance **certificate1.pem**. 

    ![Certificate in text editor](./images/09.png)

18. Upload and activate the certificates: 

    ```console
    cf custom-domain-upload-certificate-chain keyazuretemp certificate1.pem
    ```

    ![Custom domain upload certificate](./images/10.png)

    > Note: *keyazuretemp* refers to the key you have created in Step 5, certificate.pem is the name of the file that you have created in the previous step.

19. Activate the custom domain: 

    ```console
    cf custom-domain-activate keyazuretemp "*.example.com"
    ```

    ![Custom domain activation](./images/11.png)


20. Verify the custom domain activation: 

    ```console
    cf custom-domain-list
    ```

    ![Custom domain activation verification](./images/12.png)

    > Note: It can take a few minutes up to a few hours until the custom domain is activated. 

21. <a name="endpointmapping"></a>Finally, map the **SAP Launchpad service URL**  to a subdomain of your domain: 

    ```console
    cf custom-domain-map-route <launchpadservice_url> launchpad.example.com
    ```

    > Example: cf custom-domain-map-route mysubaccount.launchpad.cfapps.us20.hana.ondemand.com launchpad.saptfe-demo.com. 

    > Note: If you don't know your SAP Launchpad service URL, go back to the [previous exercise : Step 30](../03-Provisioning%20S_HANA%20Apps%20to%20Launchpad/README.md#url) - don't include the complete URL with path, just the basic URL without any path. 

    > Note: Most likely the TXT validation entry (step 14) does not need to be set again and can further be used. In this case, certbot will not ask for a renewal of the entry. 

    S/4HANA GUI apps(non-SAPUI5 based) will be opened using a different URL internally, with the runtime destination name concatenated in the URL you created in the earlier exercise. For this reason, we need to map that URL to a new subdomain.

    ```console
    cf custom-domain-map-route <s/4hana gui app url> runtimedestinationname.example.com
    ```

    > Example: cf custom-domain-map-route mysubaccount-sapdelim-runtimedestinationname.launchpad.cfapps.us20.hana.ondemand.com runtimedestinationname.example.com

    > **runtimedestinationname** here is s4hanart, which you created in [2nd exercise, step 25](../02-Configuring%20S_4HANA%20system%20for%20Content%20Federation/README.md#runtimedest)
    
22. **Execute all of the steps for the second subaccount!**

Congratulations! You have created a certificate for your domain using Certbot. With the help of the SAP Custom Domain service you registered the domain in the subaccounts in which you also provisioned SAP Launchpad service and mapped its URL to the actual domain.

Keep in mind that the Let's Encrypt certificate (the one you have created using Certbot) is only valid for three months. There are other more sophisticated alternatives for productive scenarios. 
