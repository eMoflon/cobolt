package de.tudarmstadt.maki.simonstrator.tc.underlay;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlAlgorithmID;
import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlAlgorithmParameterId;

public class UnderlayTopologyControlAlgorithms
{
   /**
    * Null implementation
    */
   public static final TopologyControlAlgorithmID MAXPOWER_TC = new UnderlayTopologyControlAlgorithmID("MAXPOWER_TC");

   /**
    * This parameter controls the aggressiveness of kTC (and its variants) w.r.t. edge weight
    * 
    * Low k -> high aggressiveness
    * High k -> low aggressiveness
    */
   public static final TopologyControlAlgorithmParameterId<Double> KTC_PARAM_K = new TopologyControlAlgorithmParameterId<Double>("k", Double.class);

   /**
    * Original version of kTC, which uses distance or RSSI as link weight
    * 
    * See also:
    * Schweizer, I., Wagner, M., Bradler, D., Mühlhäuser, M., Strufe, T.: 
    * kTC - Robust and Adaptive Wireless Ad-Hoc Topology Control. In: Proc. of the Intl. Conf. on Computer Communications and Networks (ICCCN 2012), 
    * pp. 1–9 (2012). URL https://dx.doi.org/10.1109/ICCCN.2012.6289318
    */
   public static final TopologyControlAlgorithmID D_KTC = new UnderlayTopologyControlAlgorithmID("D_KTC", Arrays.asList(KTC_PARAM_K));

   public static final TopologyControlAlgorithmParameterId<Double> E_KTC_PARAM_K = KTC_PARAM_K;;

   /**
    * Batch distance-based e-kTC
    * Kluge, R., Stein, M., Varró, G., Schürr, A., Hollick, M., Mühlhäuser, M.
    * A Systematic Approach to Constructing Families of Incremental Topology Control Algorithms Using Graph Transformation
    * In: Software and Systems Modeling (SoSyM)
    * Under Submission
    */
   public static final TopologyControlAlgorithmID E_KTC = new UnderlayTopologyControlAlgorithmID("E_KTC", Arrays.asList(KTC_PARAM_K));

   /**
    * Gabriel Graph TC algorithm
    * 
    * See also:
    * Wang, Y.: Topology control for wireless sensor networks.
    * In: Wireless Sensor Networks and Applications, Signals and Communication Technology, 
    * pp. 113–147. Springer (2008). URL https://dx.doi.org/10.1007/978-0-387-49592-7_5
    */
   public static final TopologyControlAlgorithmID GABRIEL_GRAPH = new UnderlayTopologyControlAlgorithmID("GG");

   /**
    * Relative Neighborhood Graph algorithm
    * 
    * See also:
    * Karp, B., Kung, H.T.: GPSR: Greedy perimeter stateless routing for wireless networks.
    * In: Proc. of the 6th Annual Intl. Conference on Mobile Computing and Networking
    * (MobiCom 2000), pp. 243–254. ACM(2000). 
    * URL https://dx.doi.org/10.1145/345910.345953
    */
   public static final TopologyControlAlgorithmID RELATIVE_NEIGHBORHOOD_GRAPH = new UnderlayTopologyControlAlgorithmID("RNG");

   /**
    * This parameter controls the aggressiveness of l*-kTC, l-kTC and g-kTC w.r.t. hop-count constraints
    * 
    * The idea is that routing paths to a dedicated base station should not be stretched more than a factor a
    * 
    *  Low a -> low aggressiveness
    *  High a -> high aggressiveness
    */
   public static final TopologyControlAlgorithmParameterId<Double> LSTAR_KTC_PARAM_A = new TopologyControlAlgorithmParameterId<Double>("a", Double.class);

   public static final TopologyControlAlgorithmParameterId<Double> LSTAR_KTC_PARAM_K = KTC_PARAM_K;;

   /**
    * l*-kTC - A purely local version of the l-kTC algorithm.
    * 
    * See also:
    * Stein, M., Petry, T., Schweizer, I., Bachmann, M., Mühlhäuser, M.: 
    * Topology control in wireless sensor networks: What blocks the breakthrough? 
    * In: Proc. of the Intl. Conf. on Local Computer Networks (LCN 2016), pp. 1–9 (2016)
    */
   public static final TopologyControlAlgorithmID LSTAR_KTC = new UnderlayTopologyControlAlgorithmID("LSTAR_KTC",
         Arrays.asList(KTC_PARAM_K, LSTAR_KTC_PARAM_A));

   /**
    * XTC algorithm
    * 
    * See also:
    * Wattenhofer, R., Zollinger, A.: XTC: a practical topology control algorithm for ad-hoc networks.
    * In: Proc. of the Intl. Parallel and Distributed Processing Symposium (IPDPS 2004), 
    * pp. 216–223. IEEE (2004). 
    * URL https://dx.doi.org/10.1109/IPDPS.2004.1303248
    */
   public static final TopologyControlAlgorithmID XTC = new UnderlayTopologyControlAlgorithmID("XTC");

   /**
    * The number of cones for the Yao graph
    */
   public static final TopologyControlAlgorithmParameterId<Integer> YAO_PARAM_CONE_COUNT = new TopologyControlAlgorithmParameterId<Integer>("coneCount",
         Integer.class);

   /**
    * See also:
    * Yao, A.C.C.: On constructing minimum spanning trees in k-dimensional spaces and related problems. 
    * SIAM Journal on Computing 11(4), 721–736 (1982). 
    * URL https://dx.doi.org/10.1137/0211059
    */
   public static final TopologyControlAlgorithmID YAO = new UnderlayTopologyControlAlgorithmID("Yao", Arrays.asList(YAO_PARAM_CONE_COUNT));

   /**
    * Local Minimum Spanning Tree algorithm
    * Li, Hou, Sha: "Design and analysis of an MST-based topology control algorithm," IEEE Trans. Wireless Comm., 4(3) 2005, DOI: 10.1109/TWC.2005.846971 
    */
   public static final TopologyControlAlgorithmID LMST = new UnderlayTopologyControlAlgorithmID("LMST");

   /**
    * Global Minimum Spanning Tree algorithm
    * As far as I know, this algorithm has not been published explicity but rather serves as a baseline
    */
   public static final TopologyControlAlgorithmID GMST = new UnderlayTopologyControlAlgorithmID("GMST");

   /**
    * The list of all supported algorithm underlay Topology Control IDs
    */
   private static List<TopologyControlAlgorithmID> ALGORITHMS;

   /**
    * Returns the {@link TopologyControlAlgorithmID} that has the name algorithmName
    * @param algorithmName the name to search for
    * @return the corresponding algorithm
    * @throws IllegalArgumentException if no such algorithm exists
    */
   public static TopologyControlAlgorithmID mapToTopologyControlID(final String algorithmName)
   {
      for (final TopologyControlAlgorithmID algo : getAlgorithms())
      {
         if (algo.getName().equals(algorithmName))
            return algo;
      }

      throw new IllegalArgumentException(String.format("Unsupported algorithm ID: %s. Available: %s", algorithmName,
            getAlgorithms().stream().map(TopologyControlAlgorithmID::getName).collect(Collectors.toList())));
   }

   /**
    * Returns the unmodifiable list of available algorithms
    * @return the list of algorithms
    */
   public static List<TopologyControlAlgorithmID> getAlgorithms()
   {
      return Collections.unmodifiableList(ALGORITHMS);
   }

   /**
    * The main purpose of this class is to automatically register new {@link UnderlayTopologyControlAlgorithms} in the list of algorithms {#link {@link UnderlayTopologyControlAlgorithms#ALGORITHMS}).
    * 
    * @author Roland Kluge - Initial implementation
    *
    */
   private static class UnderlayTopologyControlAlgorithmID extends TopologyControlAlgorithmID
   {
      // Internal counter of underlay TC algorithms
      private static int algorithmIdCounter = 0;

      private UnderlayTopologyControlAlgorithmID(final String name)
      {
         this(name, new ArrayList<>());
      }

      private UnderlayTopologyControlAlgorithmID(final String name, final List<TopologyControlAlgorithmParameterId<?>> parameterNames)
      {
         super(name, algorithmIdCounter++, parameterNames);
         // Needed to cope with the non-deterministic instantiation order of static members
         if (ALGORITHMS == null)
            ALGORITHMS = new ArrayList<>();
         ALGORITHMS.add(this);
      }

   }

   /**
    * Returns the appropriate {@link TopologyControlAlgorithmParameterId} for the given configuration string
    * @param name the parameter name
    * @return the corresponding {@link TopologyControlAlgorithmParameterId}
    */
   public static TopologyControlAlgorithmParameterId<?> parseTopologyControlAlgorithmParameterId(final String name)
   {
      switch (name)
      {
      case "k":
         return KTC_PARAM_K;
      case "a":
         return LSTAR_KTC_PARAM_A;
      case "coneCount":
         return YAO_PARAM_CONE_COUNT;
      default:
         throw new IllegalArgumentException(String.format("Cannot map parameter name '%s' to Topology Control parameter ID", name));
      }
   }
}
