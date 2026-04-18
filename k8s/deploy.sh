#!/bin/bash
set -e

echo "=== Deploying Olist API to Kubernetes ==="

echo "[1/7] Creating namespace..."
kubectl apply -f 00-namespace.yaml

echo "[2/7] Creating secrets and configmap..."
kubectl apply -f 01-secrets.yaml
kubectl apply -f 02-configmap.yaml

echo "[3/7] Creating persistent volumes..."
kubectl apply -f 03-pvc.yaml

echo "[4/7] Deploying databases..."
kubectl apply -f 04-postgres-app.yaml
kubectl apply -f 05-postgres-keycloak.yaml
echo "  Waiting for databases..."
kubectl wait --for=condition=ready pod -l app=postgres-app -n olist --timeout=120s
kubectl wait --for=condition=ready pod -l app=postgres-keycloak -n olist --timeout=120s

echo "[5/7] Deploying Vault and Keycloak..."
kubectl apply -f 06-vault.yaml
kubectl apply -f 08-keycloak.yaml
echo "  Waiting for Vault..."
kubectl wait --for=condition=ready pod -l app=vault -n olist --timeout=120s
kubectl apply -f 07-vault-init-job.yaml
echo "  Waiting for Keycloak..."
kubectl wait --for=condition=ready pod -l app=keycloak -n olist --timeout=300s

echo "[6/7] Deploying application..."
kubectl apply -f 09-olist-api.yaml
kubectl apply -f 10-hpa.yaml
echo "  Waiting for API..."
kubectl wait --for=condition=ready pod -l app=olist-api -n olist --timeout=300s

echo "[7/7] Configuring networking..."
kubectl apply -f 11-ingress.yaml
kubectl apply -f 12-network-policy.yaml

echo ""
echo "=== Deploy complete! ==="
echo "API:      kubectl port-forward svc/olist-api 9090:9090 -n olist"
echo "Keycloak: kubectl port-forward svc/keycloak 8080:8080 -n olist"
echo "Vault:    kubectl port-forward svc/vault 8200:8200 -n olist"
