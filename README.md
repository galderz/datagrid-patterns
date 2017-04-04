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

1. Execute `run-servers.sh`

2. Execute `delays.query.continuous.FxApp` application.
