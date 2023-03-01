#!/bin/bash

echo "=> Setup default root webapp"
$JBOSS_CLI -c << EOF
batch

# Remove default root webapp
/subsystem=undertow/server=default-server/host=default-host/location=\/:remove

# Setup filestore as default root webapp
/subsystem=undertow/server=default-server/host=default-host:write-attribute(name=default-web-module, value=filestore.war)

# Run the batch commands
run-batch

# Reload the server configuration
reload
EOF