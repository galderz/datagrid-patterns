#!/usr/bin/env bash

set -e
trap "trap - SIGTERM && kill -- -$$" SIGINT SIGTERM EXIT

SERVER_ROOT=..
SERVER_HOME=$SERVER_ROOT/infinispan-server-9.0.0.Final
CLUSTER_SIZE_MAIN="/host=master/server=server-three/subsystem=datagrid-infinispan/cache-container=clustered:read-attribute(name=cluster-size)"


function waitForClusters()
{
  MEMBERS_MAIN=''
  while [ "$MEMBERS_MAIN" != \"3\" ];
  do
    MEMBERS_MAIN=$($SERVER_HOME/bin/ispn-cli.sh -c $CLUSTER_SIZE_MAIN | grep result | tr -d '\r' | awk '{print $3}')
    echo "Waiting for clusters to form (main: $MEMBERS_MAIN)"
    sleep 3
  done
}


rm -drf $SERVER_ROOT/infinispan-server-9.0.0.Final
echo "Remove old server directory."


unzip $SERVER_ROOT/infinispan-server-9.0.0.Final-bin.zip -d $SERVER_ROOT
echo "Unzipped server"


cp server-config/domain/domain.xml $SERVER_HOME/domain/configuration
cp server-config/domain/host.xml $SERVER_HOME/domain/configuration
cp server-config/org.infinispan.main_module.xml $SERVER_HOME/modules/system/layers/base/org/infinispan/main/module.xml
echo "Configuration files copied to server."


$SERVER_HOME/bin/add-user.sh -u mgmt -p 'mypassword'
echo "Admin user added."


$SERVER_HOME/bin/domain.sh &


waitForClusters
echo "Infinispan servers started."


# Wait until script stopped
while :
do
  sleep 5
done
