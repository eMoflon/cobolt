/*
 * Copyright (c) 2005-2010 KOM - Multimedia Communications Lab
 *
 * This file is part of Simonstrator.KOM.
 *
 * Simonstrator.KOM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * PeerfactSim.KOM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PeerfactSim.KOM.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package de.tudarmstadt.maki.simonstrator.tc.graph;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import org.junit.Assert;
import org.junit.Test;

import de.tudarmstadt.maki.simonstrator.api.Graphs;
import de.tudarmstadt.maki.simonstrator.api.common.graph.BasicGraph;
import de.tudarmstadt.maki.simonstrator.api.common.graph.DirectedEdge;
import de.tudarmstadt.maki.simonstrator.api.common.graph.EdgeID;
import de.tudarmstadt.maki.simonstrator.api.common.graph.Graph;
import de.tudarmstadt.maki.simonstrator.api.common.graph.IEdge;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INode;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INodeID;
import de.tudarmstadt.maki.simonstrator.api.common.graph.Node;
import de.tudarmstadt.maki.simonstrator.tc.GraphUtil;
import de.tudarmstadt.maki.simonstrator.tc.testing.GraphTestUtil;

/**
 * Unit tests for {@link Graph}
 */
public class GraphTest {

	@Test
	public void testEdgeInducedGraph() throws Exception {
		final Node n1 = new Node(INodeID.get("n1"));
		final Node n2 = new Node(INodeID.get("n2"));
		final Node n3 = new Node(INodeID.get("n3"));
		final DirectedEdge e12 = new DirectedEdge(n1.getId(), n2.getId());
		final DirectedEdge e13 = new DirectedEdge(n1.getId(), n3.getId());
		final DirectedEdge e23 = new DirectedEdge(n2.getId(), n3.getId());

		final Graph graph = GraphUtil.createGraph(Arrays.asList(e12, e13, e23));
		Assert.assertEquals(new HashSet<>(Arrays.asList(n1, n2, n3)), new HashSet<>(graph.getNodes()));
	}

	@Test
	public void testAdjacency() throws Exception {
		final Node n1 = new Node(INodeID.get("n1"));
		final Node n2 = new Node(INodeID.get("n2"));
		final Node n3 = new Node(INodeID.get("n3"));
		final Node n4 = new Node(INodeID.get("n4"));
		final DirectedEdge e12 = new DirectedEdge(n1.getId(), n2.getId());
		final DirectedEdge e13 = new DirectedEdge(n1.getId(), n3.getId());
		final DirectedEdge e23 = new DirectedEdge(n2.getId(), n3.getId());

		final Graph g = new BasicGraph(Arrays.asList(n1, n2, n3, n4), Arrays.asList(e12, e13, e23));

		Assert.assertEquals(0, g.getOutdegree(n4.getId()));
		Assert.assertEquals(0, g.getIndegree(n4.getId()));
		Assert.assertEquals(2, g.getOutdegree(n1.getId()));
		Assert.assertEquals(0, g.getIndegree(n1.getId()));
		Assert.assertEquals(1, g.getOutdegree(n2.getId()));
		Assert.assertEquals(1, g.getIndegree(n2.getId()));
		Assert.assertEquals(0, g.getOutdegree(n3.getId()));
		Assert.assertEquals(2, g.getIndegree(n3.getId()));

		final INode n5 = g.createAndAddNode(INodeID.get("n5"));
		g.addEdge(new DirectedEdge(n5.getId(), INodeID.get("n4")));

		Assert.assertEquals(1, g.getOutdegree(n5.getId()));
		Assert.assertEquals(1, g.getIndegree(n4.getId()));

		g.removeNode(INodeID.get("non-contained node"));

		// Remove n1 - should also work for new instances of Node that have the
		// same id
		g.removeNode(INodeID.get("n1"));

		Assert.assertEquals(1, g.getOutdegree(n2.getId()));
		Assert.assertEquals(0, g.getIndegree(n2.getId()));
		Assert.assertEquals(0, g.getOutdegree(n3.getId()));
		Assert.assertEquals(1, g.getIndegree(n3.getId()));
		Assert.assertEquals(2, g.getEdgeCount());

		g.addEdge(new DirectedEdge(n3.getId(), n4.getId()));

		Assert.assertTrue(g.contains(new DirectedEdge(n3.getId(), n4.getId())));

		g.addEdge(new DirectedEdge(n3.getId(), n4.getId()));
		Assert.assertEquals(3, g.getEdgeCount());

		g.clear();

		Assert.assertEquals(0, g.getNodeCount());
		Assert.assertEquals(0, g.getEdgeCount());

	}

	@Test
	public void testBidirectional() throws Exception {
		final Node n1 = new Node(INodeID.get("n1"));
		final Node n2 = new Node(INodeID.get("n2"));
		final Node n3 = new Node(INodeID.get("n3"));
		final DirectedEdge e12 = new DirectedEdge(n1.getId(), n2.getId());
		final DirectedEdge e21 = new DirectedEdge(n2.getId(), n1.getId());
		final DirectedEdge e13 = new DirectedEdge(n1.getId(), n3.getId());
		final DirectedEdge e31 = new DirectedEdge(n3.getId(), n1.getId());
		final DirectedEdge e23 = new DirectedEdge(n2.getId(), n3.getId());
		final DirectedEdge e32 = new DirectedEdge(n3.getId(), n2.getId());

		Assert.assertFalse(GraphUtil.isSymmetric(new BasicGraph(Arrays.asList(e12, e13, e23))));
		Assert.assertTrue(GraphUtil.isSymmetric(new BasicGraph(Arrays.asList(e12, e21, e23, e32))));
		Assert.assertTrue(GraphUtil.isSymmetric(new BasicGraph(Arrays.asList(e12, e21, e13, e31, e31, e23, e23, e32))));
	}
	
	@Test
   public void testInverseEdges() throws Exception
   {
      Graph graph = Graphs.createGraph();
      final INode n1 = Graphs.createNode("n1");
      final INode n2 = Graphs.createNode("n2");
      final INode nX = Graphs.createNode("X");
      final INode nY = Graphs.createNode("Y");
      final IEdge e1 = Graphs.createDirectedEdge(n1, n2);
      final IEdge e2 = Graphs.createDirectedEdge(nX, nY);
      graph.addNode(n1);
      graph.addNode(n2);
      graph.addNode(nX);
      graph.addNode(nY);
      graph.addEdge(e1);
      graph.addEdge(e2);
      graph.makeInverseEdges(e1, e2);
      Assert.assertEquals(e1, graph.getInverseEdge(e2));
      Assert.assertEquals(e1, graph.getInverseEdge(e2.getId()));
      Assert.assertEquals(e2, graph.getInverseEdge(e1));
      Assert.assertEquals(e2, graph.getInverseEdge(e1.getId()));
      
      // Check that adding the same pair is not a problem
      graph.makeInverseEdges(e1, e2);
      graph.makeInverseEdges(e2, e1);
      
      // Second iteration but with edge IDs
      graph.clear();
      graph.addNode(n1);
      graph.addNode(n2);
      graph.addNode(nX);
      graph.addNode(nY);
      graph.addEdge(e1);
      graph.addEdge(e2);

      graph.makeInverseEdges(e1, e2);
      Assert.assertEquals(e1, graph.getInverseEdge(e2));
      Assert.assertEquals(e1, graph.getInverseEdge(e2.getId()));
      Assert.assertEquals(e2, graph.getInverseEdge(e1));
      Assert.assertEquals(e2, graph.getInverseEdge(e1.getId()));
   }
	
	@Test
   public void testGetInverseEdgesWithMultigraph() throws Exception
   {
	   final INodeID gn1 = INodeID.get(1);
      final INodeID gn2 = INodeID.get(2);
      final IEdge ge12_1 = Graphs.createDirectedWeightedEdge(gn1, gn2, 1.0);
      final IEdge ge12_2 = Graphs.createDirectedWeightedEdge(gn1, gn2, 2.0);
      final IEdge ge21 = Graphs.createDirectedWeightedEdge(gn2, gn1, 3.0);
      final Graph graph = GraphUtil.createGraph(Arrays.asList(ge12_1, ge12_2, ge21));
      graph.makeInverseEdges(ge12_1, ge21);
      graph.makeInverseEdges(ge12_2, ge21);
      
      assertSetEquals(Arrays.asList(ge12_1, ge12_2), graph.getInverseEdges(ge21));
      assertSetEquals(Arrays.asList(ge21), graph.getInverseEdges(ge12_1));
      assertSetEquals(Arrays.asList(ge21), graph.getInverseEdges(ge12_2));
   }

   @Test
   public void testMultigraphBasics() throws Exception
   {
      final INodeID n1 = INodeID.get(1);
      final INodeID n2 = INodeID.get(2);
      final IEdge e12_1 = Graphs.createDirectedWeightedEdge(n1, n2, EdgeID.get("e12_1"), 1.0);
      final IEdge e12_2 = Graphs.createDirectedWeightedEdge(n1, n2, EdgeID.get("e12_2"), 2.0);
      final Graph graph = GraphUtil.createGraph(Arrays.asList(e12_1, e12_2));
      
      GraphTestUtil.assertNodeAndEdgeCount(2, 2, graph);
      
      graph.removeEdge(e12_1);
      
      GraphTestUtil.assertNodeAndEdgeCount(2, 1, graph);
      
      graph.addEdge(e12_1);
      
      GraphTestUtil.assertNodeAndEdgeCount(2, 2, graph);
   }
   
   @Test
   public void testDirectedEdgeEquals() throws Exception
   {
      final INodeID n1 = INodeID.get(1);
      final INodeID n2 = INodeID.get(2);
      final DirectedEdge e12_1 = new DirectedEdge(n1, n2, EdgeID.get("e12_1"));
      final DirectedEdge e12_2 = new DirectedEdge(n1, n2, EdgeID.get("e12_2"));
      final DirectedEdge e12_11 = new DirectedEdge(n1, n2, EdgeID.get("e12_1"));
      
      Assert.assertNotEquals(e12_1, e12_2);
      Assert.assertEquals(e12_1, e12_11);
   }

   private <T> void assertSetEquals(final Collection<T> expected, final Collection<T> actual)
   {
      Assert.assertEquals(new HashSet<>(expected), new HashSet<>(actual));
   }
}

