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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.google.common.collect.Sets;

import de.tud.kom.p2psim.api.scenario.ConfigurationException;
import de.tud.kom.p2psim.impl.simengine.Simulator;
import de.tudarmstadt.maki.simonstrator.api.Randoms;
import edu.uci.ics.jung.graph.DirectedGraph;

/**
 * Extends a Directed Graph for the properties of activity and interactions. The
 * values for both properties will be lie between 0 and 1. This mean, that after
 * the call of {@link IGraphExtender#extendGraph}, the values are changed with
 * regard to the following scheme.
 * <p>
 * 
 * The activity will be derived after this formula:<br>
 * a = numberOfNeighbors / maxNumberOfNeighbors<br>
 * The numberOfNeighbors are the number of outgoing edges! The
 * maxNumberOfNeighbors is for every Node the same and is the maximal value in
 * the graph for numberOfNeighbors. <br>
 * Additionally we add a bonus to the lowest activity, because the activity of
 * nodes with few neighbors is higher after the paper of Wilson, Christo, et al.
 * "User interactions in social networks and their implications." (Proceedings
 * of the 4th ACM European conference on Computer systems. Acm, 2009.), because
 * there are new to the System and are very active. Nodes with an activity of
 * lower then 0.1 get a random bonus up to 0.7.
 * 
 * <p>
 * 
 * The interaction between the nodes will be derived after this criteria: <br>
 * 1) In Social Networks, there are many links with few interaction and few
 * links with high interaction! (Wilson, Christo, et al.
 * "User interactions in social networks and their implications." Proceedings of
 * the 4th ACM European conference on Computer systems. Acm, 2009.) So we
 * distribute a truncated power law from 1 to 0.01 and do this random on the
 * edges!<br>
 * 
 * 2) Reciprocity! Similar nodes attract themselves. For the similarity of
 * Nodes, we make following assumption, that a node must have at least x same
 * friends.<br>
 * Therefore the link between them should have in both direction a similar
 * value! For this, we take the higher value which has been calculated in 1).
 * <br>
 * 
 * 
 * @author Christoph Muenker
 * @version 1.0, 10.06.2013
 */
public class SimpleGraphExtender implements IGraphExtender {

	private double paretoExponent = 2;

	private long seed;

	private Random rand;

	private int numberOfSameFriends = 4;

	public SimpleGraphExtender() {
		setSeed(Simulator.getSeed());
	}

	@Override
	public DirectedGraph<SocialNode, SocialEdge> extendGraph(
			DirectedGraph<SocialNode, SocialEdge> graph) {
		int maxNeighborCount = findMaxNeighborCount(graph);
		updateActivity(graph, maxNeighborCount);

		updateInteraction(graph);

		return graph;
	}

	private void updateInteraction(DirectedGraph<SocialNode, SocialEdge> graph) {
		TruncatedPaertoDistribution tpd = new TruncatedPaertoDistribution(
				paretoExponent, 0.01, 1, rand);
		Map<SocialNode, Set<SocialNode>> neighbors = new HashMap<SocialNode, Set<SocialNode>>();

		// set the interaction to a value from the distribution
		for (SocialEdge edge : graph.getEdges()) {
			edge.setInteraction(tpd.getNext());
		}

		// performance, we add all node neighbors in a hashmap
		for (SocialNode node : graph.getVertices()) {
			Set<SocialNode> nodeNeighbor = new HashSet<SocialNode>(
					graph.getNeighbors(node));
			neighbors.put(node, nodeNeighbor);
		}

		// find neighbors with more then five same friends
		for (SocialNode source : graph.getVertices()) {
			for (SocialNode dest : graph.getVertices()) {
				if (source.equals(dest)) {
					continue;
				}
				if (getNumberOfSameFriends(neighbors, source, dest) < numberOfSameFriends) {
					continue;
				}
				// find all edges between source and dest
				Set<SocialEdge> edges = new HashSet<SocialEdge>(
						graph.findEdgeSet(source, dest));
				edges.addAll(graph.findEdgeSet(dest, source));

				assert edges.size() <= 2;

				// find max and set max
				double max = 0;
				for (SocialEdge edge : edges) {
					max = Math.max(max, edge.getInteraction());
				}

				for (SocialEdge edge : edges) {
					edge.setInteraction(max);
				}
			}
		}

	}

	private int getNumberOfSameFriends(
			Map<SocialNode, Set<SocialNode>> neighbors, SocialNode source,
			SocialNode dest) {
		Set<SocialNode> neighborsSource = neighbors.get(source);
		Set<SocialNode> neighborsDest = neighbors.get(dest);

		return Sets.union(neighborsSource, neighborsDest).size();
	}

	private void updateActivity(DirectedGraph<SocialNode, SocialEdge> graph,
			int maxNeighborCount) {
		for (SocialNode v : graph.getVertices()) {
			int neighborCount = graph.getNeighborCount(v);
			double activity = ((double) neighborCount) / maxNeighborCount;
			if (activity < 0.1) {
				activity += rand.nextDouble() * 0.7;
			}
			v.setActivity(activity);
		}
	}

	private int findMaxNeighborCount(DirectedGraph<SocialNode, SocialEdge> graph) {
		int maxNeighborCount = -1;
		for (SocialNode v : graph.getVertices()) {
			maxNeighborCount = Math.max(maxNeighborCount,
					graph.getNeighborCount(v));
		}
		return maxNeighborCount;
	}

	public void setParetoExponent(double paretoExponent) {
		this.paretoExponent = paretoExponent;
	}

	public void setSeed(long seed) {
		this.seed = seed;
		rand = Randoms.getRandom(SimpleGraphExtender.class);
		rand.setSeed(seed);
	}

	public static class TruncatedPaertoDistribution {

		private double xMin = 1;

		private double xMax;

		private double yMax;

		private double yMin;

		private double k;

		private double scalingFactor;

		private Random rand;

		public TruncatedPaertoDistribution(double paretoExponent, double yMin,
				double yMax, Random rand) {
			this.k = paretoExponent;
			this.yMin = yMin;
			this.yMax = yMax;
			this.rand = rand;

			scalingFactor = calcPareto(xMin) / this.yMax;
			xMax = calcXMax();
		}

		private double calcXMax() {
			return Math.exp(-Math.log(yMin * scalingFactor * xMin / k)
					/ (k + 1) + Math.log(xMin));
		}

		public double getNext() {
			double x = (rand.nextDouble() * (xMax - xMin)) + xMin;
			return calcPareto(x) / scalingFactor;
		}

		private double calcPareto(double x) {
			if (x < xMin) {
				return 0;
			} else {
				return k / xMin * Math.pow(xMin / x, k + 1);
			}
		}
	}

	public void setNumberOfSameFriends(int numberOfSameFriends) {
		if (numberOfSameFriends < 0) {
			throw new ConfigurationException(
					"numberOfSameFriends must be creater than 0!");
		}
		this.numberOfSameFriends = numberOfSameFriends;
	}
}
