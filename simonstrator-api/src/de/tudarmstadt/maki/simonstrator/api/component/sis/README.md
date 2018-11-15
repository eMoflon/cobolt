# The System Information Service

_Based on the MAKI-Seminar on June 3rd, given by Torsten, Nils and Bjoern._

## Introduction to the SiS Concept
The SiS (or System Information Service) has to act as a central proxy for &mdash; and gateway to &mdash; state information and measurements available on a network element. It should provide means to exchange information between multiple different mechanisms running one one node, as well as between multiple nodes running a mechanism. To this end, its tasks largely overlay with those of a classical monitoring system. The *new* SiS defined in this package, thus, acts as a gateway and information resolver and directly interacts with a monitoring system to gather required state.

### Local State vs. Local Observations
The basic data model of the SiS distinguishes between __local state__ and __local observations__. Local state refers to information that describes the local device. Examples include the current battery level or the window size of the TCP congestion window.

Local observations refers to information we have locally available about a **remote** node. Such information is usually gathered by a mechanism or protocol, for example a routing table in the Chord DHT overlay containing the measured round trip times for each contact.

If we want to obtain information that is **not yet locally available**, we start a new observation using the `SiSRequest` query model as described in the following. Usually, the answer to this request is then saved by the SiS as a local observation for some time.

### Taxonomy (or Ontology)
To describe metrics and state across multiple mechanisms on different layers, a common taxonomy is required. Furthermore, as we want to derive state information based on locally available data (e.g., the number of overlay neighbors from the current number of transport connections), we need an Ontology including such potential derivations.

To find a suitable trade-off between a generic approach and a pragmatic, actually usable way of defining types and derivations in the SiS, we introduced the generic `SiSType` and defined a number of available types in the `SiSTypes`-container.

When interacting with the SiS, an application developer makes use of the types defined in `SiSTypes` by using the static public references, e.g. 
```java 
sis.get().localState(SiSTypes.PHY_LOCATION, /* .. */);
```

A `SiSType` includes definitions of derivation functions that are used to derive the local type from other available types, as well as aggregation functions required in the monitoring system.


## Providing State for the SiS
Within an overlay or application, state is gathered to ensure correct protocol behavior. In most overlays, state is gathered in the *Neighborhood-Table* (or *Routing Table*) of the local node. Assume, that our sample overlay gathers the round trip times (RTTs) to each of its neighbors as part of its maintenance operations. Now, we want to expose this state information to the SiS.

### Register as an Information Provider

First, you need to retrieve the `SiSComponent` (and save a reference to it) from within your `HostComponent`:

```java
SiSComponent sis = getHost().getComponent(SiSComponent.class);
```

You access the `SiSInformationProvider` side of the SiS by calling `sis.provide()`. To provide a local measurement of the local node state (**local state**), you simply call:

```java
sis.provide().localNodeState(SiSType<T> type,
		SiSDataCallback<T> dataCallback,
		SiSInfoProperties informationProperties);
```

where `type` is one of the `SiSTypes`-constants. Most important part is the `SiSDataCallback`, which enables the SiS to retrieve the current value of the local node state. This callback is to be implemented by you. Additionally, you can describe the quality and type of the information by providing additional `SiSInfoProperties`. These properties are created by calling any of the methods hidden behind `sis.describe()`.

If you want to provide an **observation**. e.g., some local state you maintain about any other node, you can do so by calling:

```java
sis.provide().observedNodeState(INodeID observedNode,
		SiSType<T> type, SiSDataCallback<T> dataCallback,
		SiSInfoProperties informationProperties);
```

Again, you need to specify the `SiSType` of the information as well as a data callback and additional information properties. Furthermore, you now need to identify the node that you provide observations on. To identify a node, an `INodeID` is used. The `INodeID` is based on the `NetID` of a node (its IP-Address). As a node might have multiple IP addresses, it might also have multiple `INodeID`s - it is the responsibility of the SiS to resolve such naming issues transparent to the application.

To retrieve the `INodeID` corresponding to a given `NetID`, simply call the static method `INodeID.get(NetID netid)`.


### Stop Providing Information

The aforementioned methods return a `SiSProviderHandle` that can be used to later on revoke access to an observation or a local state information. In order to revoke access, simply call
```java
sis.provide().revoke(SiSProviderHandle handle);
```
using the provided handle. The handle is also included in every call to the `SiSDataCallback`, enabling later revocation even id the handle was not stored on registration of a new data source.


## Retrieving State from the SiS

There are numerous reasons why you would want to retrieve state information or observations from the SiS: if you overlay/service maintains neighborhoods, it may profit from already existing observations of potential neighbors (not only their RTT...). Furthermore, your mechanism might want to adapt to overall observations of the network (e.g., the average load, or the maximum movement speed...) that are collected by the monitoring mechanism. To retrieve data, you have two options: **accessing local information** or **requesting or accessing remote information**.

Both options are accessed via the `SiSInformationConsumer`-interface hidden behind `sis.get()`. 

### Accessing Local Information

In order to access local information about our own node, simply call:
```java
sis.get().localState(SiSType<T> type, SiSRequest request);
```
using a `SiSType` out of the static types provided by `SiSTypes`. Additional request properties, such as desired scope and origin of the data can be specified as part of the `SiSRequest` object, which mostly acts as a container of a `SiSInfoProperties` object. The method call immediately returns the best value the SiS can determine. If no data that matches the request can be found, an `InformationNotAvailableException` is thrown. `SiSRequest` objects are created by calling `sis.get().newRequest(SiSInfoProperties infoProperties)`.

If you want to access locally available observations of another node's state (e.g., the observed RTT to that node), simply call:
```java
sis.get().localObservationOf(INodeID observedNode, SiSType<T> type,
		SiSRequest request);
```
The method behaves exactly as `localState()` does, it just requires an additional `INodeID` to specify the node that we want to receive data on. `INodeID`s are explained in the previous section.

There exists a convenience method allowing us to retrieve all locally available observations of a given type for all nodes:
```java
sis.get().allLocalObservations(SiSType<T> type,
		SiSRequest request);
```
This method simply returns a Map of `INodeID`s and the corresponding values.

### Requesting or Accessing Remote Information

We distinguish between two kinds of remote information: **raw observations** and **aggregated observations**. Raw observations describe information that is tied to the originating node, e.g. a list of each node's RTT. Aggregated information is no longer tied to the respective nodes, but describes an aggregated view on (a part of) the network. An aggregated observation could be, for example, the average observed RTT in the whole network. Both respective access methods operate callback-based, as the SiS might need to trigger collection of remote data via monitoring before being able to answer to the query. However, results should be saved by the SiS


