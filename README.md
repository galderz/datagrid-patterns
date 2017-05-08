**Table of Contents**

- [Introduction](#introduction)
- [Pre-requisites](#pre-requisites)
- [Application Domain](#application-domain)
- [Analytics Demo](#analytics-aemo)
	- [Running Demo](#running-demo)
	- [Testsuite](#testsuite)
		- [WordCountTest](#wordcounttest)
		- [Jupyter Test](#jupyter-test)
	- [Reference](#reference)
		- [Server Configuration Explained](#server-configuration-explained)
- [Real Time Demo](#real-time-demo)
	- [Running Demo](#running-demo-1)
	- [Testsuite](#testsuite-1)
- [Live Events](#live-events)
- [Blogs](#blogs)


# Introduction 

This is a repository contaning example applications/demos on how to use Infinispan for real-time and offline analytics use cases.


# Pre-requisites

* Infinispan Server zip [file](http://infinispan.org/download/) version 9.0.0 or higher, located in a directory below this one.
* Install Jupyter by installing [Anaconda](https://www.continuum.io/downloads).
It is recommended that the Python 3.x version is installed.
* Install [Node Version Manager](https://github.com/creationix/nvm) (NVM).


# Application Domain

The demos presented in this repository use rail-based transport as theme.
The domain is constructed out of the following entities:

* `Train` - a train that transport passengers around the country, it contains:
  * A name, e.g. `ICN 531`)
  * A destination, e.g. `Basel SBB`
  * Category, e.g. `ICE` for intercity europe trains
  * An identifier.
  The identifier is a concatenation of the train name, destination and departure time.

* `Station` - represents a physical train station, it contains:
  * A numeric identifier, e.g. `8501008`
  * A name (e.g. `Basel SBB`)
  * A geographic location including latitude and longitude information. 

* `Stop` - represents a train's passing through a station.
It represents each of the entries in a given station's train board.
It contains:
  * Train information, as described above.
  * The expected departure timestamp for a given train through a station.
  * The platform where the train is expected to stop.
  * If train is delayed, the number of minutes that a train is delayed going through the station.

* `StationBoard` - represents the list of train stops through a station at a given time, contains:
  * Station board entries represented as upcoming trains to stop in this station.
  * Time when the station board information was collected.


# Analytics Demo

The aim of the analytics demo is to show how to use Infinispan extensions to Java Streams to run distributed computations.
One of the new features introduced in Infinispan 8 was distributed streams, which enhances Java Stream capabilities to run on a distributed cache.
The key aspect of these enhancements is that instead of moving around data to do any processing, Infinispan moves the functions or lambdas around the cluster so that they are executed against local data.

This is a very powerful feature since it enables Java developers to use familiar Java Streams API to do distributed computing.
This demo shows how Infinispan distributed streams can be used against remotely against cluster of Infinispan servers.

The final objective of the demo is to use historic data of train station board information to answer this question: 

> What is the time of the day when there is the biggest ratio of delayed trains?

To help answer this question, a remote cache is defined to contain historic information of all train stops keyed by train id:  

    RemoteCache<String, Stop>
    
*Note: A train goes through multiple stations and hence it has multiple stops.
For simplicity, only the last stop in terms of time is kept*

This demo uses protocol buffers to describe the types involved in the demo so that they can be unmarshalled remotely in the server.
This is necessary so that the remote server task can work with user types as opposed to binary data.
This means that when the demo starts, there are a few set up invocations:

* For each type stored, declare it as a protocol buffers message type in a `.proto` file.
* Store the `.proto` file in the Infinispan Server's protobuf metadata cache.
* Store `.proto` file and a marshaller for each of the message types in the client. 
* Store proto marshallers in server via the execution of a remote task.

Once the set up is complete, the demo will store 3 week's worth of data from station boards.
After data loading has completed, the analytics task can be executed returns the result of the distributed analytics computation to the client.
The client then stores results in an intermediate cache, which then can be consumed by a Jupyter notebook to provide a plot that answers the question posed above.

Below are more detailed instructions on how to run the demo.

## Running Demo

Starts the serves by executing:

    $ ./run-servers.sh 

It starts three Infinispan Server instances forming a domain.
Details about the configuration can be found in the reference section.

Next, deploy the server tasks in the Infinispan servers.
The first task deployed registers the protobuf marshallers that convert from binary to user types.
The second tasks is the task that will calculate the ratio of delayed trains across the day.

    $ cd analytics
    $ mvn clean install package -am -pl analytics-server
    $ mvn wildfly:deploy -pl analytics-server

Then, run the data injector, `delays.java.stream.InjectApp`, to add the data into servers.  

With data loaded, calculate the ratio of delayed trains across the day by executing `delays.java.stream.AnalyticsApp` class.

Once the data injector and the delayed trains ratio has been calculated, start the Jupyter notebook:

    $ cd analytics/analytics-jupyter
    $ ~/anaconda/bin/jupyter notebook

Open `delayed-trains-ratio.ipynb` notebook and verify that each cell calculates without an error. 
The result should show that 2am is the time of the day when there is the biggest ratio of delayed trains.


## Testsuite

`testsuite` folder contains several test projects that were developed to gain confidence in the analytics demo:

* `EmbeddedAnalyticsTest` in `test-analytics-domain` is a standalone unit test. 
It was created to verify that the statistics required for the analytics demo could be generated using Java Streams API and the application domain above.

* `JsonViaHotRodTest` in `test-analytics-client` is a unit test that that relies on a server being available on a specific port (see test).
This test also requires that the cache it interacts with has compatibility mode enabled. 
The aim of this test is that the Infinispan RemoteCache API can be used to store a JSON formatted String and this can be retrieved via REST URL (see test for details). 

### WordCountTest

`WordCounTest` in `test-analytics-client` is a unit test that checks whether remote tasks relying on non-primitive objects work.
Before running this test, it is necessary to deploy the task along with with POJOs.
This can be done using the following commands:

    $ cd testsuite
    $ mvn clean install package -am -pl test-analytics-server
    $ mvn wildfly:deploy -pl test-analytics-server

Once the server task has been deployed, execute `WordCountTest`.

### Jupyter Test

`testsuite/test-jupyter` contains a test that verifies that a Jupyter notebook can retrieve data from a RESTful JSON endpoint. 

Before running the test, it is necessary to execute the following installation steps.
Go to the `test-jupyter` directory and install `json-server`:

    $ cd testsuite/test-jupyter
    $ nvm use 4.2
    $ npm install json-server

Once the installation steps are complete, start the JSON server with one of the test files provided:

    $ ./node_modules/.bin/json-server --watch test_2000000.json
    
Start the Jupyer notebook:

    $ ~/anaconda/bin/jupyter notebook

From the Jupyter notebook, open open the `test-plot_2000000.ipynb` notebook and verify that all cells can be re-executed.

## Reference

### Server Configuration Explained

This section explains the tweaks that have been applied to the server configuration to run the analytics demo.
These changes are already automated through the `run-servers.sh` execution, and hence the user is not required to apply them manually to run the demo.
However, it's important for the reader to be aware of them in case wanting to replicate the demo somewhere else.

To run the distributed streams computation server side, the demo will execute a remote server task in one of the Infinispan servers.
Given that this remote server task requires accessing `Stop` type, which is a non-primitive type, there are some extra tweaks required in the server.

By default, remote caches store information in binary format in the server.
However, to be able to access data in non-binary format from the remote server task, it is necessary for the binary data to be unmarshalled server-side.
To do that, this demo takes advantage of Infinispan's support for types to be defined via protobuf schemas. 
For types to be unmarshalled using protobuf schemas, it's necessary for a cache's compatibility marshaller to be set to `org.infinispan.query.remote.CompatibilityProtoStreamMarshaller`. 

To be able to do that's it's necessary to add modify `$SERVER_HOME//modules/system/layers/base/org/infinispan/main/module.xml` file and add an extra module dependency:  

    <module name="org.infinispan.remote-query.server" optional="true"/>


# Real Time Demo

The aim of the real-time demo is to show how to Infinispan's Continuous Query technology can be used to track changing data.
Initially, continuous Query involves defining an query and a listener implementation.
When the query is executed, any matching data gets passed in to the listener implementation as part of the joining result set.
As more data is added or removed, the listener gets invoked with any new matches, or matches that are no longer part of the result set.

For this demo, a remote cache is defined as: 

    RemoteCache<Station, StationBoard> stationBoards...

The final objective of the demo is to present a live-updating table of delayed trains.
To help achieve this objective, a remote cache should be populated with each station's upcoming train board information at a given time.

Remote querying uses protocol buffers as common format for being able to deconstruct binary data.
So, once the remote cache has been defined, the following steps are required before the query can be defined:

* For each type stored, declare it as a protocol buffers message type in a `.proto` file.
* Store the `.proto` file in the Infinispan Server's protobuf metadata cache.
* Store `.proto` file and a marshaller for each of the message types in the client. 
 
Next, a query is defined as matching any station boards where at least one of the train stops is delayed:

    QueryFactory qf = Search.getQueryFactory(stationBoards);
    Query query = qf.from(StationBoard.class)
        .having("entries.delayMin").gt(0L)
        .build();

Once the query is defined, a continuous query listener is attached to it:

      ContinuousQueryListener<Station, StationBoard> listener = 
               new ContinuousQueryListener<Station, StationBoard>() {
         @Override
         public void resultJoining(Station key, StationBoard value) {
            // ...
         }

         @Override
         public void resultUpdated(Station key, StationBoard value) {
            // ...
         }

         @Override
         public void resultLeaving(Station key) {
            // ... 
         }
      };

      continuousQuery = Search.getContinuousQuery(stationBoards);
      continuousQuery.addContinuousQueryListener(query, listener);

When the demo application runs, it cycles through some cached station board data and injects that information to the remote cache.
As data gets updated and delayed station board entries are found, these are presented in a JavaFX based table.  

## Running Demo

Starts the serves by executing:

    $ ./run-servers.sh 

It starts three Infinispan Server instances forming a domain.
The cache that the demo interacts with is defined as being distributed with 2 copies, so it can cope with 1 server going down and still keep all data.

Next, execute `delays.query.continuous.FxApp` application.

## Testsuite

`testsuite/test-real-time` folder contains several standalone tests that were developed to gain confidence in the real-time demo.

Some of these tests exercise JavaFX capabilities (e.g. `FxTest` and `FxTaskTest`) and hence it's recommended to run those and play with the interface exposed. 
Other tests verify other aspects, such as basic remote continous queries, JSON parsing and GZIP'ing.

Finally, a CLI version of the real-time demo is available which is not as dynamic as the JavaFX version.


# Live Events
 
Here's a list of conferences and user groups where this demo has been presented.
The `live-events` folder contains step-by-step instructions of the demos, as presented in these live events:

* 6th April 2017 - Devoxx France
(
[slides](https://speakerdeck.com/galderz/patterns-dutilisation-de-systemes-in-memory)
|
[video](https://www.youtube.com/watch?v=ATh1PuTho-M)
|
[live demo steps](live-events/devoxxFr17.md)
)


# Blogs

Here's a list of blog posts where this demo has been featured:

* 7th April 2017 - [In Memory Data Grid Patterns Demos from Devoxx France!](http://blog.infinispan.org/2017/04/in-memory-data-grid-patterns-demos-from.html) 
