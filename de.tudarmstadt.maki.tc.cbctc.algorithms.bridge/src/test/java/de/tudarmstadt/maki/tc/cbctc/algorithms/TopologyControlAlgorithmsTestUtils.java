package de.tudarmstadt.maki.tc.cbctc.algorithms;

import org.junit.Assert;

import de.tudarmstadt.maki.simonstrator.tc.facade.ITopologyControlFacade;
import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlAlgorithmParamters;
import de.tudarmstadt.maki.simonstrator.tc.underlay.UnderlayTopologyControlAlgorithms;
import de.tudarmstadt.maki.tc.cbctc.algorithms.facade.EMoflonFacade;
import de.tudarmstadt.maki.tc.cbctc.model.Edge;

/**
 * Test utilities for the (model-based) topology control algorithms
 * 
 * @author Roland Kluge - Initial implementation
 *
 */
public final class TopologyControlAlgorithmsTestUtils
{
   private TopologyControlAlgorithmsTestUtils()
   {
      throw new UnsupportedOperationException();
   }

   /**
    * Returns the path to the distance-related test case with the given index
    * 
    * @param i the index
    * @return the project-relative path to the test case
    */
   public static String getPathToDistanceTestGraph(final int i)
   {
      return "instances/testgraph_D" + i + ".grapht";
   }

   /**
    * Returns the path to the energy-related test case with the given index
    * 
    * @param i the index
    * @return the project-relative path to the test case
    */
   public static String getPathToEnergyTestGraph(final int i)
   {
      return "instances/testgraph_E" + i + ".grapht";
   }

   /**
    * Returns the path to the hop-count-related test case with the given index
    * 
    * @param i the index
    * @return the project-relative path to the test case
    */
   public static String getPathToHopCountTestGraph(final int i)
   {
      return "instances/testgraph_H" + i + ".grapht";
   }
   
   /**
    * Returns the path to the angle-related test case with the given index
    * 
    * @param i the index
    * @return the project-relative path to the test case
    */
   public static String getPathToAngleTestGraph(final int i)
   {
      return "instances/testgraph_A" + i + ".grapht";
   }

   /**
    * Asserts that the weight attribute of each edge in the facade's topology is set, i.e., unequal to EMoflonFacade#DEFAULT_VALUE_FOR_UNDEFINED_ATTRIBUTES
    * @param facade
    */
   public static void assertWeightSet(final EMoflonFacade facade)
   {
      // @formatter:off
      final Edge edgeWithUnsetWeight = facade.getTopology().getEdges().stream()
            .filter(e -> new Double(e.getWeight()).equals(new Double(EMoflonFacade.DEFAULT_VALUE_FOR_UNDEFINED_ATTRIBUTES)))
            .findAny().orElse(null);
      // @formatter:on
      Assert.assertNull(String.format("The weight of the following edge is not set: '%s'", edgeWithUnsetWeight), edgeWithUnsetWeight);
   }

   /**
    * Invokes the given facade that must be configured to run a kTC-style algorithm with the given k-value.
    * 
    * @param facade the given facade
    * @param k see {@link UnderlayTopologyControlAlgorithms#KTC_PARAM_K}
    */
   public static void runFacadeKTC(ITopologyControlFacade facade, double k)
   {
      facade.run(TopologyControlAlgorithmParamters.create(UnderlayTopologyControlAlgorithms.KTC_PARAM_K, k));
   }

   /**
    * Configures the given facade to run {@link UnderlayTopologyControlAlgorithms#LSTAR_KTC} 
    * @param facade the facade
    * @param k see {@link UnderlayTopologyControlAlgorithms#KTC_PARAM_K}
    * @param a see {@link UnderlayTopologyControlAlgorithms#LSTAR_KTC_PARAM_A}
    */
   public static void runFacadeLStarKTC(ITopologyControlFacade facade, double k, double a)
   {
      facade.run(
            TopologyControlAlgorithmParamters.create(UnderlayTopologyControlAlgorithms.KTC_PARAM_K, k, UnderlayTopologyControlAlgorithms.LSTAR_KTC_PARAM_A, a));
   }

   /**
    * Configures the given facade to run {@link UnderlayTopologyControlAlgorithms#YAO} 
    * @param facade the facade
    * @param coneCount see {@link UnderlayTopologyControlAlgorithms#YAO_PARAM_CONE_COUNT}
    */
   public static void runFacadeYao(ITopologyControlFacade facade, int coneCount)
   {
      facade.run(TopologyControlAlgorithmParamters.create(UnderlayTopologyControlAlgorithms.YAO_PARAM_CONE_COUNT, coneCount));
   }

}