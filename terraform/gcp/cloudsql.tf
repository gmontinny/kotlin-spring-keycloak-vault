resource "google_sql_database_instance" "app" {
  name             = "${var.project_name}-app-pg"
  database_version = "POSTGRES_16"
  region           = var.region

  settings {
    tier              = "db-f1-micro"
    availability_type = "ZONAL"
    disk_size         = 10
    disk_autoresize   = true

    ip_configuration {
      ipv4_enabled    = false
      private_network = google_compute_network.main.id
    }

    backup_configuration {
      enabled                        = true
      start_time                     = "03:00"
      point_in_time_recovery_enabled = false
      backup_retention_settings {
        retained_backups = 7
      }
    }

    user_labels = {
      project     = var.project_name
      environment = var.environment
    }
  }

  deletion_protection = false

  depends_on = [google_service_networking_connection.private]
}

resource "google_sql_database" "app" {
  name     = "olist_db"
  instance = google_sql_database_instance.app.name
}

resource "google_sql_user" "app" {
  name     = var.db_username
  instance = google_sql_database_instance.app.name
  password = var.db_password
}

resource "google_sql_database_instance" "keycloak" {
  name             = "${var.project_name}-kc-pg"
  database_version = "POSTGRES_16"
  region           = var.region

  settings {
    tier              = "db-f1-micro"
    availability_type = "ZONAL"
    disk_size         = 10
    disk_autoresize   = true

    ip_configuration {
      ipv4_enabled    = false
      private_network = google_compute_network.main.id
    }

    backup_configuration {
      enabled                        = true
      start_time                     = "03:00"
      point_in_time_recovery_enabled = false
      backup_retention_settings {
        retained_backups = 7
      }
    }

    user_labels = {
      project     = var.project_name
      environment = var.environment
    }
  }

  deletion_protection = false

  depends_on = [google_service_networking_connection.private]
}

resource "google_sql_database" "keycloak" {
  name     = "keycloak_db"
  instance = google_sql_database_instance.keycloak.name
}

resource "google_sql_user" "keycloak" {
  name     = var.kc_db_username
  instance = google_sql_database_instance.keycloak.name
  password = var.kc_db_password
}
