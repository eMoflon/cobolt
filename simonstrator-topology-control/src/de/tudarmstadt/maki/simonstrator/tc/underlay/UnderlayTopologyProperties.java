package de.tudarmstadt.maki.simonstrator.tc.underlay;

import de.tudarmstadt.maki.simonstrator.api.common.graph.EdgeProperty;
import de.tudarmstadt.maki.simonstrator.api.common.graph.GraphElementProperty;
import de.tudarmstadt.maki.simonstrator.api.common.graph.NodeProperty;
import de.tudarmstadt.maki.simonstrator.api.component.sis.type.SiSType;

/**
 * This interface collects typical properties of underlay topologies
 *
 */
public interface UnderlayTopologyProperties
{

   /**
    * Topology control state of an edge
    *
    * See {@link EdgeState} for details.
    */
   SiSType<EdgeState> EDGE_STATE = new EdgeProperty<>("edgeState", EdgeState.class);

   /**
    * Remaining energy of a node
    */
   SiSType<Double> REMAINING_ENERGY = new NodeProperty<>("remainingEnergy", Double.class);

   /**
    * Required transmission power of an edge
    */
   SiSType<Double> REQUIRED_TRANSMISSION_POWER = new EdgeProperty<>("requiredTransmissionPower", Double.class);

   /**
    * Length of an edge or distance of a node (from a specific reference point)
    */
   SiSType<Double> DISTANCE = new EdgeProperty<>("distance", Double.class);

   /**
    * Generic weight of an edge
    */
   SiSType<Double> WEIGHT = new EdgeProperty<>("underlayEdgeWeight", Double.class);

   /**
    * Angle of an edge (usually in degree)
    */
   SiSType<Double> ANGLE = new EdgeProperty<>("angle", Double.class);

   /**
    * Expected lifetime of a node w.r.t. a specific edge
    */
   SiSType<Double> EXPECTED_LIFETIME_PER_EDGE = new EdgeProperty<>("expectedLifetimePerEdge", Double.class);

   /**
    * Hop count (of a node)
    */
   SiSType<Integer> HOP_COUNT = new GraphElementProperty<>("hopCount", Integer.class);

   /**
    * Longitude (of a node). Also known as x coordinate.
    */
   SiSType<Double> LONGITUDE = new GraphElementProperty<>("longitude", Double.class);

   /**
    * Longitude (of a node). Also known as y coordinate.
    */
   SiSType<Double> LATITUDE = new GraphElementProperty<>("latitude", Double.class);

   /**
    * The local view (in hops) of this node
    */
   SiSType<Integer> LOCAL_VIEW_HORIZON = new GraphElementProperty<>("localViewHorizon", Integer.class);

   /**
    * Nodes that are a base station should possess this property (with value true)
    */
   SiSType<Boolean> BASE_STATION_PROPERTY = new NodeProperty<>("isBaseStation", Boolean.class);

}
