output "resource_group_name" {
  description = "Resource group name"
  value       = azurerm_resource_group.main.name
}

output "aks_cluster_name" {
  description = "AKS cluster name"
  value       = azurerm_kubernetes_cluster.main.name
}

output "aks_kubeconfig_command" {
  description = "Command to configure kubectl"
  value       = "az aks get-credentials --resource-group ${azurerm_resource_group.main.name} --name ${azurerm_kubernetes_cluster.main.name}"
}

output "postgres_app_fqdn" {
  description = "PostgreSQL app server FQDN"
  value       = azurerm_postgresql_flexible_server.app.fqdn
}

output "postgres_keycloak_fqdn" {
  description = "PostgreSQL Keycloak server FQDN"
  value       = azurerm_postgresql_flexible_server.keycloak.fqdn
}

output "acr_login_server" {
  description = "ACR login server"
  value       = azurerm_container_registry.main.login_server
}
