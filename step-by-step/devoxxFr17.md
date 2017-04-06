# Pre talk

1. Start servers executing: 


    $ cd ~/0/events/devoxxFr17/datagrid-patterns
    $ ./run-servers.sh

2. Deploy server tasks:

    
    $ cd 0/events/devoxxFr17/datagrid-patterns/analytics
    $ mvn clean install package -am -pl analytics-server
    $ mvn wildfly:deploy -pl analytics-server

2. From IDE, run InjectApp program

3. Start Jupyter notebook:


    $ cd ~/0/events/devoxxFr17/datagrid-patterns/analytics/analytics-jupyter
    $ ~/anaconda/bin/jupyter notebook

Then, open live-demo.ipynb file

4. Open these files in tabs: DelayRatioTask, AnalyticsApp, FxTask, StationBoard


# Analytics Demo

* Describe the use case: Swiss rail transport system
* Talk about the domain (use image of station board to describe it)
* Talk about question we're trying to find answer for
* Talk about how the RemoteCache is defined: RemoteCache<String, Stop>
  * Data grid of 3 nodes loaded with 3 weeks' worth of data 
* Focus on speaking about the remote server task, how the lambda gets distributed, that it gets executed in single node who compiles all results
* Explain how results are sent back to client who stores them in an intermediate cache as JSON

## Actions

1. Execute AnalyticsApp from IDE
2. Go Jupyter notebook and execute each cell individually


# Continuous Query Demo

* Describe the use case, same domain as previous demo
* The aim is to create a dashboard of delayed trains
* Talk about how the RemoteCache is defined: RemoteCache<Station, StationBoard>
* Show StationBoard POJO and note how it contains a List<Stop> and how the query matches into any Stop who's delay is bigger than 0

## Actions

1. Execute FxApp from IDE and see delays being reported
