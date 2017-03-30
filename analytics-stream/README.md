# Testing Server Tasks

This folder contains certain test classes to verify that server tasks that rely on user defined POJOs work as expected.

Following are the steps required to run this test:

1. Execute `./run-servers.sh` from the directory where this script is located.

2. From the directory where this document is located, execute:
 
    $ mvn clean install package -am -pl tasks-server && mvn wildfly:deploy -pl tasks-server
    
3. Execute `test.WordCountTest` within `tasks-client`