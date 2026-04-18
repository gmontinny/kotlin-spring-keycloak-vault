resource "azurerm_postgresql_flexible_server" "app" {
  name                   = "${var.project_name}-app-pg"
  location               = azurerm_resource_group.main.location
  resource_group_name    = azurerm_resource_group.main.name
  version                = "16"
  administrator_login    = var.db_username
  administrator_password = var.db_password
  storage_mb             = 32768
  sku_name               = "B_Standard_B1ms"
  zone                   = "1"

  delegated_subnet_id = azurerm_subnet.db.id
  private_dns_zone_id = azurerm_private_dns_zone.postgres.id

  backup_retention_days        = 7
  geo_redundant_backup_enabled = false

  depends_on = [azurerm_private_dns_zone_virtual_network_link.postgres]

  tags = {
    Project     = var.project_name
    Environment = var.environment
  }
}

resource "azurerm_postgresql_flexible_server_database" "app" {
  name      = "olist_db"
  server_id = azurerm_postgresql_flexible_server.app.id
  charset   = "UTF8"
  collation = "en_US.utf8"
}

resource "azurerm_postgresql_flexible_server" "keycloak" {
  name                   = "${var.project_name}-kc-pg"
  location               = azurerm_resource_group.main.location
  resource_group_name    = azurerm_resource_group.main.name
  version                = "16"
  administrator_login    = var.kc_db_username
  administrator_password = var.kc_db_password
  storage_mb             = 32768
  sku_name               = "B_Standard_B1ms"
  zone                   = "1"

  delegated_subnet_id = azurerm_subnet.db.id
  private_dns_zone_id = azurerm_private_dns_zone.postgres.id

  backup_retention_days        = 7
  geo_redundant_backup_enabled = false

  depends_on = [azurerm_private_dns_zone_virtual_network_link.postgres]

  tags = {
    Project     = var.project_name
    Environment = var.environment
  }
}

resource "azurerm_postgresql_flexible_server_database" "keycloak" {
  name      = "keycloak_db"
  server_id = azurerm_postgresql_flexible_server.keycloak.id
  charset   = "UTF8"
  collation = "en_US.utf8"
}
