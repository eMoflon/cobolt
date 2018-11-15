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
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import de.tudarmstadt.maki.simonstrator.api.Graphs;
import de.tudarmstadt.maki.simonstrator.api.common.graph.BasicGraph;
import de.tudarmstadt.maki.simonstrator.api.common.graph.Graph;
import de.tudarmstadt.maki.simonstrator.api.common.graph.IEdge;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INodeID;
import de.tudarmstadt.maki.simonstrator.api.common.graph.Node;
import de.tudarmstadt.maki.simonstrator.tc.GraphUtil;
import de.tudarmstadt.maki.simonstrator.tc.graph.algorithm.TarjanSCC;

public class TarjanSCCTest {
	@SuppressWarnings("unused")
	@Test
	public void testFromWebsite() throws Exception {
		final Node[] nodes = new Node[13];
		for (int i = 0; i < 13; ++i) {
			nodes[i] = Graphs.createNode(i);
		}

		final List<IEdge> edges = new ArrayList<>();
		edges.add(Graphs.createDirectedEdge(nodes[4], nodes[2]));
		edges.add(Graphs.createDirectedEdge(nodes[2], nodes[3]));
		edges.add(Graphs.createDirectedEdge(nodes[3], nodes[2]));
		edges.add(Graphs.createDirectedEdge(nodes[6], nodes[0]));
		edges.add(Graphs.createDirectedEdge(nodes[0], nodes[1]));
		edges.add(Graphs.createDirectedEdge(nodes[2], nodes[0]));
		edges.add(Graphs.createDirectedEdge(nodes[11], nodes[12]));
		edges.add(Graphs.createDirectedEdge(nodes[12], nodes[9]));
		edges.add(Graphs.createDirectedEdge(nodes[9], nodes[10]));
		edges.add(Graphs.createDirectedEdge(nodes[9], nodes[11]));
		edges.add(Graphs.createDirectedEdge(nodes[7], nodes[9]));
		edges.add(Graphs.createDirectedEdge(nodes[10], nodes[12]));
		edges.add(Graphs.createDirectedEdge(nodes[11], nodes[4]));
		edges.add(Graphs.createDirectedEdge(nodes[4], nodes[3]));
		edges.add(Graphs.createDirectedEdge(nodes[3], nodes[5]));
		edges.add(Graphs.createDirectedEdge(nodes[6], nodes[8]));
		edges.add(Graphs.createDirectedEdge(nodes[8], nodes[6]));
		edges.add(Graphs.createDirectedEdge(nodes[5], nodes[4]));
		edges.add(Graphs.createDirectedEdge(nodes[0], nodes[5]));
		edges.add(Graphs.createDirectedEdge(nodes[6], nodes[4]));
		edges.add(Graphs.createDirectedEdge(nodes[6], nodes[9]));
		edges.add(Graphs.createDirectedEdge(nodes[7], nodes[6]));

		final Graph graph = new BasicGraph(Arrays.asList(nodes), edges);

		Assert.assertEquals(22, graph.getEdgeCount());

		final TarjanSCC tarjanSCC = new TarjanSCC(graph);
		final Iterable<Set<INodeID>> sccs = tarjanSCC.getSCCs();
		for (final Set<INodeID> scc : sccs) {
//			System.out.println(scc);
		}

		Assert.assertFalse(GraphUtil.isStronglyConnected(graph));
	}
}
