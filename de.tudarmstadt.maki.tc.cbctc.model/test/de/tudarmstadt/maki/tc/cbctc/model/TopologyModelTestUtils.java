package de.tudarmstadt.maki.tc.cbctc.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;

import de.tudarmstadt.maki.simonstrator.tc.testing.TopologyControlTestHelper;
import de.tudarmstadt.maki.tc.cbctc.model.constraints.ConstraintViolationReport;
import de.tudarmstadt.maki.tc.cbctc.model.constraints.ConstraintsFactory;
import de.tudarmstadt.maki.tc.cbctc.model.constraints.EdgeStateBasedConnectivityConstraint;
import de.tudarmstadt.maki.tc.cbctc.model.constraints.TopologyConstraint;

/**
 * Test utilities for the topology model
 * 
 * @author Roland Kluge - Initial implementation
 *
 */
public class TopologyModelTestUtils
{
   private static final double EPS_6 = TopologyControlTestHelper.EPS_6;

   private static final double EPS_0 = TopologyControlTestHelper.EPS_0;

   private TopologyModelTestUtils()
   {
      throw new UnsupportedOperationException("Utility class");
   }

   public static void assertState(final Topology topology, final EdgeState state, final boolean checkSymmetry, final String... edgeIds)
   {
      for (final String edgeId : edgeIds)
      {
         final Edge edge = topology.getEdgeById(edgeId);
         TopologyModelTestUtils.assertState(edge, state);
         if (checkSymmetry)
         {
            TopologyModelTestUtils.assertState(edge.getReverseEdge(), state);
         }
      }
   }

   public static void assertState(final Edge edge, final EdgeState state)
   {
      Assert.assertNotNull(edge);
      final EdgeState actualState = edge.getState();
      Assert.assertSame("Expected edge '" + edge.getId() + "' to be '" + state + "' but was '" + actualState + "'", state, actualState);
   }

   /**
    * Asserts that the given edge is in state {@link EdgeState#ACTIVE}
    */
   public static void assertActive(final Edge edge)
   {
      TopologyModelTestUtils.assertState(edge, EdgeState.ACTIVE);
   }

   /**
    * Asserts that the given edge is in state {@link EdgeState#INACTIVE}
    */
   public static void assertInactive(final Edge edge)
   {
      TopologyModelTestUtils.assertState(edge, EdgeState.INACTIVE);
   }

   /**
    * Asserts that the given edge is in state {@link EdgeState#UNCLASSIFIED}
    */
   public static void assertUnclassified(final Edge edge)
   {
      TopologyModelTestUtils.assertState(edge, EdgeState.UNCLASSIFIED);
   }

   /**
    * Asserts that all edges in the given topology that have one of the edgeIds
    * are in state {@link EdgeState#ACTIVE}
    */
   public static void assertActive(final Topology topology, final String... edgeIds)
   {
      assertState(topology, EdgeState.ACTIVE, false, edgeIds);
   }

   /**
    * Asserts that all edges in the given topology that have one of the edgeIds
    * are in state {@link EdgeState#INACTIVE}
    */
   public static void assertInactive(final Topology topology, final String... edgeIds)
   {
      assertState(topology, EdgeState.INACTIVE, false, edgeIds);
   }

   /**
    * Asserts that all edges in the given topology that have one of the edgeIds
    * are in state {@link EdgeState#UNCLASSIFIED}
    */
   public static void assertUnclassified(final Topology topology, final String... edgeIds)
   {
      assertState(topology, EdgeState.UNCLASSIFIED, false, edgeIds);
   }

   public static void assertActiveSymmetric(final Topology topology, final String... edgeIds)
   {
      assertState(topology, EdgeState.ACTIVE, true, edgeIds);
   }

   public static void assertInactiveSymmetric(final Topology topology, final String... edgeIds)
   {
      assertState(topology, EdgeState.INACTIVE, true, edgeIds);
   }

   public static void assertUnclassifiedSymmetric(final Topology topology, final String... edgeIds)
   {
      assertState(topology, EdgeState.UNCLASSIFIED, true, edgeIds);
   }

   public static void assertClassified(final Topology topology)
   {
      for (final Edge edge : topology.getEdges())
      {
         Assert.assertNotSame(EdgeState.UNCLASSIFIED, edge.getState());
      }
   }

   /**
    * Asserts that the graph contains for each edge its reverse edge and that
    * the state of forward and reverse edge are the same.
    */
   public static void assertIsStatewiseSymmetric(final Topology graph)
   {
      for (final Edge edge : graph.getEdges())
      {
         Assert.assertNotNull("Link '" + edge.getId() + "' has no reverse edge", edge.getReverseEdge());
         Assert.assertSame("Reverse edge of reverse edge of '" + edge.getId() + "' is '" + edge.getReverseEdge().getReverseEdge() + "'.", edge,
               edge.getReverseEdge().getReverseEdge());

         Assert.assertEquals(edge.getState(), edge.getReverseEdge().getState());
      }
   }

   /**
    * Asserts that all edges in the given graph are of state
    * {@edges EdgeState#ACTIVE}, except for those edges that have an ID in the
    * specified list of inactiveEdgeIds.
    */
   public static void assertActiveWithExceptions(final Topology topology, final boolean assumeSymmetricEdges, final String... inactiveEdgeIds)
   {
      final List<String> sortedInactiveEdgeIds = new ArrayList<>(Arrays.asList(inactiveEdgeIds));
      Collections.sort(sortedInactiveEdgeIds);
      for (final Edge edge : topology.getEdges())
      {
         if (Collections.binarySearch(sortedInactiveEdgeIds, edge.getId()) >= 0)
         {
            assertInactive(edge);
         } else if (assumeSymmetricEdges && Collections.binarySearch(sortedInactiveEdgeIds, edge.getReverseEdge().getId()) >= 0)
         {
            assertInactive(edge);
         } else
         {
            assertActive(edge);
         }
      }
   }

   /**
    * Asserts that all edges in the given graph are of state
    * {@edges EdgeState#UNCLASSIFIED}
    */
   public static void assertUnclassified(final Topology graph)
   {
      for (final Edge edge : graph.getEdges())
      {
         assertUnclassified(edge);
      }
   }

   public static void assertAllActiveSymmetricWithExceptions(final Topology graph, final String... edgeIds)
   {
      assertActiveWithExceptions(graph, true, edgeIds);
   }

   /**
    * Asserts that the active-edges-induced subgraph of the given graph is
    * connected.
    */
   public static void assertActiveConnectivity(Topology graph)
   {
      assertConnectivity(graph, EdgeState.ACTIVE);
   }

   /**
    * Asserts that the subgraph induced by active and unclassified edges of the
    * given graph is connected.
    */
   public static void assertClassifiedConnectivity(Topology graph)
   {
      assertConnectivity(graph, EdgeState.ACTIVE, EdgeState.UNCLASSIFIED);
   }

   /**
    * Asserts that the subgraph induced by the edges of the given states of the
    * given graph is connected.
    */
   public static void assertConnectivity(Topology graph, EdgeState... states)
   {
      final EdgeStateBasedConnectivityConstraint constraint = ConstraintsFactory.eINSTANCE.createEdgeStateBasedConnectivityConstraint();
      constraint.getStates().addAll(Arrays.asList(states));
      assertTopologyConstraints(graph, Arrays.asList(constraint));
   }

   /**
    * Asserts that the given graph fulfills all given constraints
    */
   public static void assertTopologyConstraints(Topology graph, List<? extends TopologyConstraint> constraints)
   {
      final ConstraintViolationReport report = ConstraintsFactory.eINSTANCE.createConstraintViolationReport();
      for (final TopologyConstraint constraint : constraints)
      {
         constraint.checkOnTopology(graph, report);
         Assert.assertEquals("Constraint checker report contains violations", 0, report.getViolations().size());
      }

   }

   public static void assertTopologyConstraint(Topology graph, TopologyConstraint constraint)
   {
      final ConstraintViolationReport report = ConstraintsFactory.eINSTANCE.createConstraintViolationReport();
      constraint.checkOnTopology(graph, report);
      Assert.assertEquals("Constraint checker report contains violations", 0, report.getViolations().size());
   }

   public static void assertNodeAndEdgeCount(Topology topology, int nodeCount, int edgeCount)
   {
      Assert.assertEquals(nodeCount, topology.getNodeCount());
      Assert.assertEquals(edgeCount, topology.getEdgeCount());
   }

   public static void assertEdgeDistance(final Topology topology, final String id, final double distance)
   {
      Assert.assertEquals("Distance mismatch of " + id + ".", distance, topology.getEdgeById(id).getDistance(), TopologyModelTestUtils.EPS_0);
   }

   public static void assertEdgeWeight(final Topology topology, final String id, final double weight)
   {
      Assert.assertEquals("Weight mismatch of " + id + ".", weight, topology.getEdgeById(id).getWeight(), TopologyModelTestUtils.EPS_0);
   }

   public static void assertEquals6(final double expected, final double actual)
   {
      Assert.assertEquals(expected, actual, EPS_6);
   }

   public static void assertEquals0(final double expected, final double actual)
   {
      Assert.assertEquals(expected, actual, EPS_0);
   }

}
