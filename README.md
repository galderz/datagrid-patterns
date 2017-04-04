# Introduction 

This is a repository contaning example applications/demos on how to use Infinispan for real-time and offline analytics use cases.


# Pre-requisites

* Infinispan Server zip [file](http://infinispan.org/download/) version 9.0.0 or higher, located in a directory below this one.


# Application Domain

The demos presented in this repository use rail-based transport as theme.
The domain is constructed out of the following entities:

* Train: A train that transport passengers around the country.
It contains:
  * A name, e.g. `ICN 531`)
  * A destination, e.g. `Basel SBB`
  * Category, e.g. `ICE` for intercity europe trains
  * An identifier.
  The identifier is a concatenation of the train name, destination and departure time.

* Station: Represents a physical train station.
It contains:
  * A numeric identifier, e.g. `8501008`
  * A name (e.g. `Basel SBB`)
  * A geographic location including latitude and longitude information. 

* Stop: Represents a train's passing through a station.
It represents each of the entries in a given station's train board.
It contains:
  * Train information, as described above.
  * The expected departure timestamp for a given train through a station.
  * The platform where the train is expected to stop.
  * If train is delayed, the number of minutes that a train is delayed going through the station.

* Station Board: Represents the list of train stops through a station at a given time.
It contains:
  * Station board entries represented as upcoming trains to stop in this station.
  * Time when the station board information was collected.


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

      ContinuousQueryListener<Station, StationBoard> listener = new ContinuousQueryListener<Station, StationBoard>() {
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

## Running Real Time Demo

1. Execute `run-servers.sh`. 
It starts three Infinispan Server instances forming a domain.
The cache that the demo interacts with is defined as being distributed with 2 copies, so it can cope with 1 server going down and still keep all data.

2. Execute `delays.query.continuous.FxApp` application.
