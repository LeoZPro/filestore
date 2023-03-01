#!/bin/bash

echo "=> Setup OIDC secure deployment"
$JBOSS_CLI -c << EOF
batch

# Create the secure deployment
/subsystem=elytron-oidc-client/secure-deployment=ROOT.war:add(provider-url=$OIDC_PROVIDER_URL,ssl-required=none,client-id=$OIDC_CLIENT_ID,public-client=true,confidential-port=0)

# Run the batch commands
run-batch

# Reload the server configuration
reload
EOF
