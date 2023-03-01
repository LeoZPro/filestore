#!/bin/bash

echo "=> Setup JMS notification topic"
$JBOSS_CLI -c << EOF
batch

# Create the notification topic
/subsystem=messaging-activemq/server=default/jms-topic=notification:add(entries=[java:/jms/topic/notification])

# Run the batch commands
run-batch

# Reload the server configuration
reload
EOF
