variable "subscription_id" {}
variable "tenant_id" {}
variable "profile_name" {}
variable "resourcegroup" {}
variable "region_1" {}
variable "region_2" {}
variable "endpoint_1" {}
variable "endpoint_2" {}
variable "authorization_1" {}
variable "authorization_2" {}

provider "azurerm" {
  subscription_id = var.subscription_id
  tenant_id       = var.tenant_id
  features {}
}

resource "azurerm_resource_group" "resource-group-global" {
  name     = var.resourcegroup
  location = "West Europe"
}

resource "azurerm_traffic_manager_profile" "traffic-manager" {
  name                   = var.profile_name
  resource_group_name    = azurerm_resource_group.resource-group-global.name
  traffic_routing_method = "Priority"

  dns_config {
    relative_name = var.profile_name
    ttl           = 1
  }

  monitor_config {
    protocol                     = "https"
    port                         = 443
    path                         = "/http/ping"
    interval_in_seconds          = 30
    timeout_in_seconds           = 10
    tolerated_number_of_failures = 2
    expected_status_code_ranges  = ["200-200"]
  }


}

# Create Traffic Manager - West End Point
resource "azurerm_traffic_manager_endpoint" "tm-endpoint-1" {
  name                = "Cloud Integration ${var.region_1}"
  resource_group_name = azurerm_resource_group.resource-group-global.name
  profile_name        = azurerm_traffic_manager_profile.traffic-manager.name
  type                = "externalEndpoints"
  target              = var.endpoint_1
  priority            = 1
  custom_header {
    name  = "Authorization"
    value = "Basic ${var.authorization_1}"
  }
}

# Create Traffic Manager - West End Point
resource "azurerm_traffic_manager_endpoint" "tm-endpoint-2" {
  name                = "Cloud Integration US"
  resource_group_name = azurerm_resource_group.resource-group-global.name
  profile_name        = azurerm_traffic_manager_profile.traffic-manager.name
  type                = "externalEndpoints"
  target              = var.endpoint_2
  priority            = 2
  custom_header {
    name  = "Authorization"
    value = "Basic ${var.authorization_2}"
  }
}
