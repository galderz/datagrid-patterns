# Introduction 

This is a repository contaning example applications/demos on how to use Infinispan for real-time and offline analytics use cases.


# Pre-requisites

* Infinispan Server zip [file](http://infinispan.org/download/) version 9.0.0 or higher, located in a directory below this one.


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


# Real Time Demo

The aim of the real-time demo is to show how to Infinispan's Continuous Query technology can be used to track changing data.
Initially, continuous Query involves defining an query and a listener implementation.
When the query is executed, any matching data gets passed in to the listener implementation as part of the joining result set.
As more data is added or removed, the listener gets invoked with any new matches, or matches that are no longer part of the result set.

For this demo, a remote cache is defined as: 

    RemoteCache<Station, StationBoard> stationBoards...

The aim is for the remote cache to track each station's station board state at a given time.

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

1. Execute `run-servers.sh`. 
It starts three Infinispan Server instances forming a domain.
The cache that the demo interacts with is defined as being distributed with 2 copies, so it can cope with 1 server going down and still keep all data.

2. Execute `delays.query.continuous.FxApp` application.

## Testsuite

`testsuite/test-real-time` folder contains several standalone tests that were developed to gain confidence in the real-time demo.

Some of these tests exercise JavaFX capabilities (e.g. `FxTest` and `FxTaskTest`) and hence it's recommended to run those and play with the interface exposed. 
Other tests verify other aspects, such as basic remote continous queries, JSON parsing and GZIP'ing.

Finally, a CLI version of the real-time demo is available which is not as dynamic as the JavaFX version.

## TODOS

- [ ] Remove duplicates train id duplicates from dashboard
- [ ] Implement CQ result set leaving
- [ ] Implement CQ result set updating
