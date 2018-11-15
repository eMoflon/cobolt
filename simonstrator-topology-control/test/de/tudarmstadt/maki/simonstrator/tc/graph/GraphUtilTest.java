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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import de.tudarmstadt.maki.simonstrator.api.Graphs;
import de.tudarmstadt.maki.simonstrator.api.common.graph.DirectedEdge;
import de.tudarmstadt.maki.simonstrator.api.common.graph.Graph;
import de.tudarmstadt.maki.simonstrator.api.common.graph.IEdge;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INode;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INodeID;
import de.tudarmstadt.maki.simonstrator.api.common.graph.Node;
import de.tudarmstadt.maki.simonstrator.tc.GraphUtil;

/**
 * Unit tests for {@link GraphUtil}
 *
 * @author rkluge
 *
 */
public class GraphUtilTest {

	@Test
	public void testThatRootNodeHasDepth0() throws Exception {
		final INode node = createNode("n1");
		final Graph graph = GraphUtil.createGraph(Arrays.asList(node), new ArrayList<DirectedEdge>());

		final Map<INodeID, Integer> nodeDepths = GraphUtil.calculateNodeDepths(node.getId(), graph);

		Assert.assertEquals(0, nodeDepths.get(node.getId()).intValue());
	}

	@Test
	public void testThatUnreachableNodeHasInfiniteDistance() throws Exception {
		final INode n1 = createNode("n1");
		final INode n2 = createNode("n2");
		final Graph graph = GraphUtil.createGraph(Arrays.asList(n1, n2), new ArrayList<DirectedEdge>());

		final Map<INodeID, Integer> nodeDepths = GraphUtil.calculateNodeDepths(n1.getId(), graph);

		Assert.assertEquals(GraphUtil.INFINITE_DISTANCE, nodeDepths.get(n2.getId()).intValue());
	}

	@Test
	public void testWithTwoPaths() throws Exception {
		final Node n1 = createNode("n1");
		final Node n2 = createNode("n2");
		final Node n3 = createNode("n3");
		final Node n4 = createNode("n4");
		final IEdge e12 = Graphs.createDirectedEdge(n1, n2);
		final IEdge e13 = Graphs.createDirectedEdge(n1, n3);
		final IEdge e23 = Graphs.createDirectedEdge(n2, n3);
		final IEdge e24 = Graphs.createDirectedEdge(n2, n4);
		final Graph graph = GraphUtil.createGraph(Arrays.asList(n1, n2, n3, n4), Arrays.asList(e12, e13, e23, e24));

		final Map<INodeID, Integer> nodeDepths = GraphUtil.calculateNodeDepths(n1.getId(), graph);

		Assert.assertEquals(0, nodeDepths.get(n1.getId()).intValue());
		Assert.assertEquals(1, nodeDepths.get(n2.getId()).intValue());
		Assert.assertEquals(1, nodeDepths.get(n3.getId()).intValue());
		Assert.assertEquals(2, nodeDepths.get(n4.getId()).intValue());
	}

	@Test
	public void testWith3x3Grid() throws Exception {
		final Node n11 = createNode("n1,1");
		final Node n12 = createNode("n1,2");
		final Node n13 = createNode("n1,3");
		final Node n21 = createNode("n2,1");
		final Node n22 = createNode("n2,2");
		final Node n23 = createNode("n2,3");
		final Node n31 = createNode("n3,1");
		final Node n32 = createNode("n3,2");
		final Node n33 = createNode("n3,3");
		final IEdge e1 = Graphs.createDirectedEdge(n11, n12);
		final IEdge e2 = Graphs.createDirectedEdge(n12, n13);
		final IEdge e3 = Graphs.createDirectedEdge(n21, n22);
		final IEdge e4 = Graphs.createDirectedEdge(n22, n23);
		final IEdge e5 = Graphs.createDirectedEdge(n31, n32);
		final IEdge e6 = Graphs.createDirectedEdge(n32, n33);
		final IEdge e7 = Graphs.createDirectedEdge(n11, n21);
		final IEdge e8 = Graphs.createDirectedEdge(n21, n31);
		final IEdge e9 = Graphs.createDirectedEdge(n12, n22);
		final IEdge e10 = Graphs.createDirectedEdge(n22, n23);
		final IEdge e11 = Graphs.createDirectedEdge(n13, n23);
		final IEdge e12 = Graphs.createDirectedEdge(n23, n33);

		final Graph graph = GraphUtil.createGraph(Arrays.asList(n11, n12, n13, n21, n22, n23, n31, n32, n33),
				Arrays.asList(e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12));

		final Map<INodeID, Integer> nodeDepths = GraphUtil.calculateNodeDepths(n11.getId(), graph);

		Assert.assertEquals(0, nodeDepths.get(n11.getId()).intValue());
		Assert.assertEquals(1, nodeDepths.get(n12.getId()).intValue());
		Assert.assertEquals(2, nodeDepths.get(n13.getId()).intValue());
		Assert.assertEquals(1, nodeDepths.get(n21.getId()).intValue());
		Assert.assertEquals(2, nodeDepths.get(n22.getId()).intValue());
		Assert.assertEquals(3, nodeDepths.get(n23.getId()).intValue());
		Assert.assertEquals(2, nodeDepths.get(n31.getId()).intValue());
		Assert.assertEquals(3, nodeDepths.get(n32.getId()).intValue());
		Assert.assertEquals(4, nodeDepths.get(n33.getId()).intValue());
	}

	private static Node createNode(final String id) {
		return new Node(INodeID.get(id));
	}

	public static class IsolatedNodesTests {
		@Test
		public void testNoIsolatedNodes() throws Exception {
			final Graph graph = GraphUtil.createGraph(Arrays.asList(Graphs.createDirectedEdge(INodeID.get("n1"), INodeID.get("n2"))));

			Assert.assertEquals(0, Iterables.size(GraphUtil.findIsolatedNodes(graph)));
		}

		@Test
		public void testIsolatedNodes1() throws Exception {
			final Node n1 = new Node(INodeID.get("n1"));
			final Node n2 = new Node(INodeID.get("n2"));
			final Node n3 = new Node(INodeID.get("n3"));
			final Graph graph = GraphUtil.createGraph(Arrays.asList(n1, n2, n3), Arrays.asList(Graphs.createDirectedEdge(n1, n2)));

			Assert.assertEquals(Sets.newHashSet(n3.getId()), Sets.newHashSet(GraphUtil.findIsolatedNodes(graph)));
		}
	}
}
