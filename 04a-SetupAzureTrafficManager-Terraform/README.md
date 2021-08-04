# Introduction

In this step, you will configure Azure Traffic Manager (actually the Azure Traffic Manager profile). The Azure Traffic Manager profile is the key component in this *intelligent routing* scenario, as it defines which SAP Cloud Integration tenant should be used when based on certain rules and policies. 

This is the alternative step to "Configure Azure Traffic Manager using the Azure Portal" - you can also configure the Azure Traffic Manager profile using the Azure Portal instead of using terraform. 

## Setup Azure Traffic Manager profile

1. Install Terraform on your machine. Find the instructions [here](https://learn.hashicorp.com/tutorials/terraform/install-cli#install-terraform)  
   
2. Install the Azure CLI. Find the instructions [here](https://docs.microsoft.com/en-us/cli/azure/install-azure-cli). 
3. Login to your Azure subscription using the Azure CLI: 

```cmd
az login
```
If the CLI can open your default browser, it will do so and load an Azure sign-in page.

Otherwise, open a browser page at https://aka.ms/devicelogin and enter the authorization code displayed in your terminal.

If no web browser is available or the web browser fails to open, use device code flow with az login --use-device-code.

Sign in with your account credentials in the browser.

4. Clone this repository: 

```cmd
git clone https://github.com/SAP-samples/btp-cloud-integration-intelligent-routing.git
```

5. Change into the right directory: 

```cmd
cd TODO
```
6. adjust Terraform variables
7. replace cloud integration endpoints and basic authorization
8. init terraform
9.  plan terraform
10. apply terraform
11. check Azure Portal 
12. configure DNS Zone
13. etc.