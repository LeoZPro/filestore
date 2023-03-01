#!/bin/bash

export JBOSS_CLI=$WILDFLY_HOME/bin/jboss-cli.sh

if [ ! -f wildfly.configured ]; then
function wait_for_server() {
  until `$JBOSS_CLI -c "ls /deployment" &> /dev/null`; do
    echo "Waiting"
    sleep 1
  done
}

source $WILDFLY_HOME/bin/setup_admin_password.sh

mkdir -p /tmp/deployments
mv $DEPLOYMENTS_DIR/* /tmp/deployments

echo "=> Starting WildFly server"
$WILDFLY_HOME/bin/standalone.sh -b=0.0.0.0 -c standalone-full.xml > /dev/null &

source $WILDFLY_HOME/bin/download_postgresql_driver.sh

echo "=> Waiting for the server to boot"
#wait_for_server
echo "=> Waiting is end..."

source $WILDFLY_HOME/bin/setup_datasource.sh
source $WILDFLY_HOME/bin/setup_jms.sh
source $WILDFLY_HOME/bin/setup_oidc.sh

echo "=> Shutdown Wildfly"
$JBOSS_CLI -c ":shutdown"

mv /tmp/deployments/* $DEPLOYMENTS_DIR
rm -rf /tmp/deployments
mkdir -p $FILESTORE_HOME

touch wildfly.configured
fi

echo "=> Start Wildfly"
$WILDFLY_HOME/bin/standalone.sh -b=0.0.0.0 -c standalone-full.xml