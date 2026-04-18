#!/bin/bash
echo "=== Removing Olist API from Kubernetes ==="
kubectl delete namespace olist --ignore-not-found
echo "=== Done ==="
