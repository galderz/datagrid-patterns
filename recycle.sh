#!/usr/bin/env bash

SERVER_PATH=..

# Kill any server instances running
pkill -9 -f ".*/java .* org.jboss.as.standalone .*"

# Remove old server version
rm -drf $SERVER_PATH/infinispan-server-9.0.0.CR3

# Unzip new version
unzip $SERVER_PATH/infinispan-server-9.0.0.CR3-bin.zip -d $SERVER_PATH

# Start new server
$SERVER_PATH/infinispan/bin/standalone.sh &
