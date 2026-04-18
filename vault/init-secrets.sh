#!/bin/sh

export VAULT_ADDR='http://127.0.0.1:8200'
export VAULT_TOKEN='vault-root-token'

vault kv put secret/keycloak-oauth \
  spring.datasource.username=olist_user \
  spring.datasource.password=olist_pass \
  spring.security.oauth2.client.registration.keycloak.client-id=olist-client \
  spring.security.oauth2.client.registration.keycloak.client-secret=olist-client-secret \
  spring.security.oauth2.client.registration.google.client-id=YOUR_GOOGLE_CLIENT_ID_HERE \
  spring.security.oauth2.client.registration.google.client-secret=YOUR_GOOGLE_CLIENT_SECRET_HERE

echo "Vault secrets initialized successfully!"
