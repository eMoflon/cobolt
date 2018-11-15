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

import java.util.Random;

import org.apache.commons.collections15.Factory;

import de.tud.kom.p2psim.impl.simengine.Simulator;
import de.tudarmstadt.maki.simonstrator.api.Randoms;
import de.tudarmstadt.maki.simonstrator.api.util.XMLConfigurableConstructor;
import edu.uci.ics.jung.algorithms.transformation.DirectionTransformer;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;

/**
 * This class generates a Graph with the help of JUNGs Graph Generator for a
 * Kleinberg Small World. The edges will be undirected! So, you have a relation
 * in both direction.
 * <p>
 * 
 * Note: This Generator can <b>not</b> return an extended Graph! To create a
 * extended Graph, please use a subclass of {@link IGraphExtender}.
 * 
 * @author Christoph Muenker
 * @version 1.0, 08.06.2013
 */
public class KleinbergSmallWorldGenerator implements IGraphLoader {

	private double clusteringExponent = 2;

	private Random rand;

	private long seed;

	public KleinbergSmallWorldGenerator() {
		setSeed(Simulator.getSeed());
	}

	@XMLConfigurableConstructor({ "clusteringExponent" })
	public KleinbergSmallWorldGenerator(double clusteringExponent) {
		this.clusteringExponent = clusteringExponent;
		setSeed(Simulator.getSeed());
	}

	@Override
	public DirectedGraph<SocialNode, SocialEdge> getGraph(int numberOfNodes) {

		// Kleinberg creates a lattice, which is x*y big. So we need a good
		// approximation for the numberOfNodes!
		int[] discSqrt = sqrtDiscreter(numberOfNodes);
		int rowCount = Math.max(discSqrt[0], 2);
		int colCount = Math.max(discSqrt[1], 2);

		edu.uci.ics.jung.algorithms.generators.random.KleinbergSmallWorldGenerator<SocialNode, SocialEdge> kswg = new edu.uci.ics.jung.algorithms.generators.random.KleinbergSmallWorldGenerator<SocialNode, SocialEdge>(
				new UGraphFactory(), new NodeFactory(), new EdgeFactory(),
				rowCount, colCount, clusteringExponent, true);
		kswg.setRandom((Random) rand);
		Graph<SocialNode, SocialEdge> uGraph = kswg.create();

		// transform the undirected Graph to a directed Graph
		DirectionTransformer dt = new DirectionTransformer();
		DirectedGraph<SocialNode, SocialEdge> graph = (DirectedGraph<SocialNode, SocialEdge>) dt
				.toDirected(uGraph, new DGraphFactory(), new EdgeFactory(),
						true);

		// delete nodes, which are to many
		while (graph.getVertexCount() > numberOfNodes) {
			int i = 0;
			int random = rand.nextInt(graph.getVertexCount());
			SocialNode toDelete = null;
			for (SocialNode v : graph.getVertices()) {
				if (i == random) {
					toDelete = v;
					break;
				}
				i++;
			}
			graph.removeVertex(toDelete);
		}

		return graph;
	}

	/**
	 * Tries to discret a sqrt of the given value x, that both values have the
	 * same dimension and the multiply of them is smallest bigger then x.
	 * 
	 * @param x
	 * @return Two integer values, where their multiply is bigger then x.
	 */
	private int[] sqrtDiscreter(int x) {
		int a = (int) Math.sqrt(x);
		int b = (int) Math.sqrt(x);
		boolean lastA = false;
		while (a * b < x) {
			if (!lastA) {
				lastA = true;
				a += 1;
			} else {
				b += 1;
			}
		}
		return new int[] { a, b };
	}

	@Override
	public boolean isExtendedGraph() {
		// This class cannot generate an extended Graph!
		return false;
	}

	public void setClusteringExponent(double clusteringExponent) {
		this.clusteringExponent = clusteringExponent;
	}

	public void setSeed(long seed) {
		this.seed = seed;
		rand = Randoms.getRandom(KleinbergSmallWorldGenerator.class);
		rand.setSeed(seed);
	}

	protected class UGraphFactory implements
			Factory<UndirectedGraph<SocialNode, SocialEdge>> {

		@Override
		public UndirectedGraph<SocialNode, SocialEdge> create() {

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
