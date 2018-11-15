# Optimal WSN configurations
(aka. WSN traces)

This file documents the parameter space of the WSN parameter sweeping experiments.

* Configuration: *config/rkluge/wsntraces/wsntraces.xml*
* Executor: ```de.tudarmstadt.maki.simonstrator.peerfact.application.sensor.wsntraces.WSNTraceEvaluationExecutor```
* Analyzer: ```de.tudarmstadt.maki.simonstrator.peerfact.application.sensor.wsntraces.WSNTracesAnalyzer```

## Column Specification
All columns starting with *meta-* contain metadata.
All columns starting with *f* contain feature variable values (*fs-*: system features, *fc-*: context features).
All columns starting with *m-* contain metric values.

* Metadata
    * **meta-config** Internal ID of the configuration (unique per batch of simulations).
    * **meta-seed** Seed of the random number generator of the simulator
* System features
    * **fs-TCAlgo** Selected topology control (TC) algorithm.
      A TC algorithm uses the current physical topology as input topology 
      (= nodes and their possible communication links if each node transmits with maximum power) and returns an output topology where each link is labeled according to its importance for fulfilling the desired consistency properties (e.g., connectivity, coverage of an observed area). An *active* link is required and an *inactive* link is not required for fulfilling the consistency properties.
      (For detailed explanations see [https://link.springer.com/article/10.1007%2Fs10270-017-0587-8] and [https://dx.doi.org/10.1016/j.jvlc.2016.10.003])
        * **MAXPOWER:** The output topology is identical to the input topology.
        * **D_KTC:** Classic kTC algorithm (link weight: distance, parameter: k).
        * **E_KTC:** Energy-aware variant of kTC (link weight: estimated remaining lifetime, parameter: k).
        * **LSTAR_KTC:** Routing-aware variant of kTC (link weight: distance, parameters: k,a). For details, see [https://doi.org/10.1109/LCN.2016.67].
        * **GG:** Gabriel Graph. See https://dx.doi.org/10.1007/978-0-387-49592-7_5
        * **RNG:** Relative Neighborhood Graph. See https://dx.doi.org/10.1145/345910.345953
        * **XTC:** (= kTC with k=1). See https://dx.doi.org/10.1109/IPDPS.2004.1303248
        * **YAO:** Yao graph (paramter: coneCount). See https://dx.doi.org/10.1137/0211059
        * **GMST:** Global minimum spanning tree (link weight: distance, no parameters). 
            The algorithm constructs a minimum spanning tree of the whole topology and activates exactly those links that are on the tree. (since 2017-08-01)
        * **LMST:** Local minimum spanning tree (link weight: distance, parameter: size of local view = 2 by default). 
            Each node constructs a minimum spanning tree within its local view (e.g., all nodes that are at most two hops away). 
            Afterwards, only the state of incident links of the node is modified: Incident links on the tree are activated, all other incident links are inactivated. (since 2017-08-01) 
    * **fs-WOpt** Generic weight-based optimization for topology control algorithms.
        *  **NaN**  means that this feature is deselected.
        * **0** means that the feature is selected but no links are filtered.
        * **20** means that all links with a weight of at most 20 are always active and are not being considered by the TC algorithm.
    * **fs-TCInt** Time (in minutes) between executions of the TC algorithm.
* Context features
    * **fc-MobSpeed** Average speed (in m/s) of nodes.
        * **0** means that the network is static (e.g., an environment monitoring or wildfire alarm system).
        * **1.5** is a typical pace of pedestrians.
    * **fc-Scenario** The scenario defines the communication pattern of the nodes.
        * **POINTTOPOINT:** In this scenario, random pairs of nodes are selected as source and target for transmitting one or more messages.
        * **GOSSIP:** The network is flooded with messages. When receiving a message, a node forwards it in the next iteration with a certain probability.
        * **DATACOLLECTION:** All nodes periodically send a data point (1 message) to a dedicated base station.
    * **fc-TopologyNodeCount** = node count in the initial topology (since 2017-08-01)
    * **fc-TopologyEdgeCount** = node count in the initial topology (since 2017-08-01)
    * **fc-TopologyDensity** = average node degree in the input topology
* Metrics
   * Energy consumption metrics
        * **m-EMean** = mean energy consumption of all nodes 
        * **m-EMedian** = median energy consumption
        * **m-EStddev** = standard dev. of energy consumption
        * **m-EJain** = Jain fairness of energy consumption
   * Lifetime metrics
        * **m-L1** = 1-lifetime = time (in sim. min.) until the first node goes offline
        * **m-L25p**, **m-L50p**, **m-L75p**, **m-L100p**: 25%-/50%-/100%-lifetime = time (in sim. min.) after which 25%/50%/75%/100% of all nodes are offline.
           * If, at termination of the simulation, more than 25%/.../75% of the nodes are online, we set the corresponding lifetime value to the simulation duration. 
        * **m-LJain**: Jain fairness of lifetime.
   * Connection metrics
        * **m-DropRate**: average drop rate (per underlay link, i.e., when transmitting a message from a node to one of its neighbors).
        * **m-EndToEndDropRate**: average drop rate on transport layer (i.e., end-to-end reliability).
        * **m-EndToEndLatency**: average latency
   * Topology metrics
        * **m-TopologyStorageSpanner:** Fraction of output topology size vs. input topology size (size = node count + link count). Average over all TC iterations. (since 2017-08-01)
        * **m-TopologyDensitySpanner:** Fraction of avg. node degree in output topology vs. input topology. Average over all TC iterations. (since 2017-08-01)
        * **m-HopSpannerMean:** Fraction of mean path length between any two nodes in output topology vs. input topology. Average over all TC iterations. (since 2017-08-01)
        * **m-HopSpannerMax:** Fraction of max. path length between any two nodes in output topology vs. input topology. Average over all TC iterations. (since 2017-08-01)
   
## Bugfixes
* 2017-08-01
    * Proper calculation of fc-TopologyDensity for MAXPOWER_TC
    * More useful lifetime values: For example, previously, we set m-L100p to 0 if at least one node was alive at the end of the simulation.
        Now, in this case, we set m-L100p to the end time of the simulation.
        Analogously, we set m-L1, m-L25p,... to the end time of the simulation if all nodes, at least 25% of the nodes, ... are alive at the end of the simulation.
         