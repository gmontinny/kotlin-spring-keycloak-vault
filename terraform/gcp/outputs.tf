output "gke_cluster_name" {
  description = "GKE cluster name"
  value       = google_container_cluster.main.name
}

output "gke_kubeconfig_command" {
  description = "Command to configure kubectl"
  value       = "gcloud container clusters get-credentials ${google_container_cluster.main.name} --zone ${var.zone} --project ${var.project_id}"
}

output "cloudsql_app_ip" {
  description = "Cloud SQL app instance private IP"
  value       = google_sql_database_instance.app.private_ip_address
}

output "cloudsql_keycloak_ip" {
  description = "Cloud SQL Keycloak instance private IP"
  value       = google_sql_database_instance.keycloak.private_ip_address
}

output "artifact_registry_url" {
  description = "Artifact Registry URL"
  value       = "${var.region}-docker.pkg.dev/${var.project_id}/${google_artifact_registry_repository.app.repository_id}"
}
