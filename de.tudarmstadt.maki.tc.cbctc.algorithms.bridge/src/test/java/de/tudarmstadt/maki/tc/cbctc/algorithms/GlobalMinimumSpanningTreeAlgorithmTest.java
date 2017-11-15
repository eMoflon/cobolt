package de.tudarmstadt.maki.tc.cbctc.algorithms;

import static de.tudarmstadt.maki.tc.cbctc.algorithms.TopologyControlAlgorithmsTestUtils.getPathToDistanceTestGraph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import de.tudarmstadt.maki.tc.cbctc.algorithms.io.TopologyModelGraphTReader;
import de.tudarmstadt.maki.tc.cbctc.model.Edge;
import de.tudarmstadt.maki.tc.cbctc.model.ModelFactory;
import de.tudarmstadt.maki.tc.cbctc.model.Node;
import de.tudarmstadt.maki.tc.cbctc.model.Topology;
import de.tudarmstadt.maki.tc.cbctc.model.TopologyModelTestUtils;
import de.tudarmstadt.maki.tc.cbctc.model.derivedfeatures.EdgeWeightProviders;

/**
 * Unit tests for {@link GlobalMinimumSpanningTreeAlgorithm}
 * @author Roland Kluge - Initial implementation
 *
 */
public class GlobalMinimumSpanningTreeAlgorithmTest
{

   private Topology topology;

   private GlobalMinimumSpanningTreeAlgorithm algorithm;

   private List<Node> nodes;

   @Before
   public void setUp()
   {
      this.topology = ModelFactory.eINSTANCE.createTopology();
      this.algorithm = AlgorithmsFactory.eINSTANCE.createGlobalMinimumSpanningTreeAlgorithm();
      this.nodes = new ArrayList<>();
      for (int i = 1; i <= 20; ++i)
      {
         Node node = ModelFactory.eINSTANCE.createNode();
         node.setId("n" + i);
         this.nodes.add(node);
      }
   }

   @Test
   public void testRun_EmptyTopology() throws Exception
   {
      algorithm.runOnTopology(topology);
   }

   @Test
   public void testRun_IsolatedNodes() throws Exception
   {
      addNodes(20);
      algorithm.runOnTopology(topology);
   }

   @Test
   public void testRun_DirectedTriangle() throws Exception
   {
      addNodes(3);
      final Edge e12 = createDirectedEdge(1, 2, 2.0);
      final Edge e13 = createDirectedEdge(1, 3, 3.0);
      final Edge e23 = createDirectedEdge(2, 3, 5.0);
      algorithm.runOnTopology(topology);
      Arrays.asList(e12, e13).forEach(edge -> TopologyModelTestUtils.assertActive(edge));
      Arrays.asList(e23).forEach(edge -> TopologyModelTestUtils.assertInactive(edge));
   }

   @Test
   public void testRun_UndirectedTriangle() throws Exception
   {
      addNodes(3);
      final Edge e12 = createUndirectedEdge(1, 2, 2.0);
      final Edge e13 = createUndirectedEdge(1, 3, 3.0);
      final Edge e23 = createUndirectedEdge(2, 3, 5.0);
      algorithm.runOnTopology(topology);
      Arrays.asList(e12, e13).forEach(edge -> {
         TopologyModelTestUtils.assertActive(edge);
         TopologyModelTestUtils.assertActive(edge.getReverseEdge());
      });
      Arrays.asList(e23).forEach(edge -> {
         TopologyModelTestUtils.assertInactive(edge);
         TopologyModelTestUtils.assertInactive(edge.getReverseEdge());
      });
   }

   @Test
   public void testRun_D8() throws Exception
   {
      final TopologyModelGraphTReader reader = new TopologyModelGraphTReader();
      reader.read(this.topology, getPathToDistanceTestGraph(8));
      EdgeWeightProviders.apply(this.topology, EdgeWeightProviders.DISTANCE_PROVIDER);
      algorithm.runOnTopology(topology);
      Arrays.asList("e1-2", "e1-7", "e2-3", "e3-4", "e3-10", "e4-5", "e5-6", "e8-9", "e9-10", "e10-11").stream()//
            .map(id -> topology.getEdgeById(id))//
            .forEach(edge -> {
               TopologyModelTestUtils.assertActive(edge);
               TopologyModelTestUtils.assertActive(edge.getReverseEdge());
            });
      TopologyModelTestUtils.assertClassified(this.topology);
   }

   private Edge createDirectedEdge(int i, int j, double d)
   {
      final Edge edge = topology.addDirectedEdge("e" + i + "" + j, this.nodes.get(i - 1), this.nodes.get(j - 1));
      edge.setWeight(d);
      return edge;
   }

   private Edge createUndirectedEdge(int i, int j, double weight)
   {
      final Edge edge = topology.addUndirectedEdge("e" + i + "" + j, "e" + j + "" + i, this.nodes.get(i - 1), this.nodes.get(j - 1));
      edge.setWeight(weight);
      edge.getReverseEdge().setWeight(weight);
      return edge;
   }

   private void addNodes(int n)
   {
      for (int i = 0; i < n; ++i)
      {
         topology.getNodes().add(this.nodes.get(i));
      }
   }
}
