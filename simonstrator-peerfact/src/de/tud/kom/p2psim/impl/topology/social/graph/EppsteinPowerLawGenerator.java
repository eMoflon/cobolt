/*
 * Copyright (c) 2005-2010 KOM – Multimedia Communications Lab
 *
 * This file is part of PeerfactSim.KOM.
 * 
 * PeerfactSim.KOM is free software: you can redistribute it and/or modify
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

package de.tud.kom.p2psim.impl.topology.social.graph;

import org.apache.commons.collections15.Factory;

import de.tud.kom.p2psim.impl.simengine.Simulator;
import edu.uci.ics.jung.algorithms.transformation.DirectionTransformer;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;

/**
 * This class generates a Graph with the help of JUNGs Graph Generator for a
 * Eppstein Power Law Graph. The edges will be undirected! So, you have a
 * relation in both direction.<br>
 * The Parameter edgeFactor gives the number of edges in relation to the number
 * of Vertexes. If you have x Vertexes, then will be the number of edges =
 * x*edgeFactor.
 * <p>
 * 
 * Note: This Generator can <b>not</b> return an extended Graph! To create a
 * extended Graph, please use a subclass of {@link IGraphExtender}.
 * 
 * @author Christoph Muenker
 * @version 1.0, 12.06.2013
 */
public class EppsteinPowerLawGenerator implements IGraphLoader {
	// number of reorder of edges
	private int r = 1000000;

	// how much edges
	private double edgeFactor = 3;

	private long seed;

	public EppsteinPowerLawGenerator() {
		setSeed(Simulator.getSeed());
	}

	@Override
	public DirectedGraph<SocialNode, SocialEdge> getGraph(int numberOfNodes) {
		edu.uci.ics.jung.algorithms.generators.random.EppsteinPowerLawGenerator<SocialNode, SocialEdge> generator = new edu.uci.ics.jung.algorithms.generators.random.EppsteinPowerLawGenerator<SocialNode, SocialEdge>(
				new UGraphFactory(), new NodeFactory(), new EdgeFactory(),
				numberOfNodes, (int) (numberOfNodes * edgeFactor), r);
		generator.setSeed(seed);

		Graph<SocialNode, SocialEdge> uGraph = generator.create();

		// transform the undirected Grraph to a directed Grpah
		DirectionTransformer dt = new DirectionTransformer();
		DirectedGraph<SocialNode, SocialEdge> graph = (DirectedGraph<SocialNode, SocialEdge>) dt
				.toDirected(uGraph, new DGraphFactory(), new EdgeFactory(),
						true);

		return graph;
	}

	@Override
	public boolean isExtendedGraph() {
		return false;
	}

	@Override
	public void setSeed(long seed) {
		this.seed = seed;
	}

	public void setEdgeFactor(double edgeFactor) {
		this.edgeFactor = edgeFactor;
	}

	protected class UGraphFactory implements
			Factory<Graph<SocialNode, SocialEdge>> {

		@Override
		public Graph<SocialNode, SocialEdge> create() {

			return new UndirectedSparseGraph<SocialNode, SocialEdge>();
		}

	}

	protected class DGraphFactory implements
			Factory<DirectedGraph<SocialNode, SocialEdge>> {

		@Override
		public DirectedGraph<SocialNode, SocialEdge> create() {

			return new DirectedSparseGraph<SocialNode, SocialEdge>();
		}

	}

	protected class NodeFactory implements Factory<SocialNode> {

		@Override
		public SocialNode create() {
			return new SocialNode();
		}

	}

	protected class EdgeFactory implements Factory<SocialEdge> {
		@Override
		public SocialEdge create() {
			return new SocialEdge();
		}
	}

}
