resource "google_artifact_registry_repository" "app" {
  location      = var.region
  repository_id = "${var.project_name}-api"
  format        = "DOCKER"
  description   = "Docker repository for Olist API"

  cleanup_policies {
    id     = "keep-last-10"
    action = "KEEP"
    most_recent_versions {
      keep_count = 10
    }
  }

  depends_on = [google_project_service.apis]
}
