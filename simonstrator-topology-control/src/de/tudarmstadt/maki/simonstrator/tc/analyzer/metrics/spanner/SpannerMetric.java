/*
 * Copyright (c) 2005-2010 KOM - Multimedia Communications Lab
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

package de.tudarmstadt.maki.simonstrator.tc.analyzer.metrics.spanner;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.panayotis.gnuplot.JavaPlot;

import de.tudarmstadt.maki.simonstrator.api.common.graph.GenericGraphElementProperties;
import de.tudarmstadt.maki.simonstrator.api.common.graph.Graph;
import de.tudarmstadt.maki.simonstrator.api.common.graph.IEdge;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INode;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INodeID;
import de.tudarmstadt.maki.simonstrator.tc.analyzer.javaplot.DefaultPlots;
import de.tudarmstadt.maki.simonstrator.tc.analyzer.metrics.Metric;
import de.tudarmstadt.maki.simonstrator.tc.analyzer.writer.PropertyWriter;

/**
 *
 * @author Michael Stein
 */
public abstract class SpannerMetric implements Metric {

	private double valueAveragePairwise, valueMaxPairwise;

	private ArrayList<Double> toTargetSpanningValues;

	private ArrayList<Double> pairwiseSpanningValues;

	//////
	// Mapping from nodes to array index
	///// (is required because FloydWarshall
	////// performs on array)

	BiMap<INodeID, Integer> nodeToIndex = null;
	BiMap<Integer, INodeID> indexToNode = null;

	private void initIndex(Graph graph) {
		nodeToIndex = HashBiMap.create();
		indexToNode = nodeToIndex.inverse();
		int index = 0;
		for (INode node : graph.getNodes()) {
			addToIndex(node.getId(), index++);
		}
	}

	private void addToIndex(INodeID node, int index) {
		nodeToIndex.put(node, index);
	}

	protected int getIndex(final INodeID node) {
		return nodeToIndex.get(node);
	}

	protected INodeID getNode(int index) {
		return indexToNode.get(index);
	}

	// Floyd-Warshall:
	// http://en.wikipedia.org/wiki/Floyd%E2%80%93Warshall_algorithm
	// distance/adjacency matrix provided:
	// dis is initialized with edge weight -> dis[u][v] = edge weight of edge
	// (u,v), Double.INFINITY if it doesn't exist, dist[v][v] = 0
	private void getAllPairShortestPaths(final double[][] dist) {
		for (int k = 0; k < dist.length; k++) {
			for (int i = 0; i < dist.length; i++) {
				for (int j = 0; j < dist.length; j++) {
					if (dist[i][j] > (dist[i][k] + dist[k][j])) {
						dist[i][j] = dist[i][k] + dist[k][j];
					}
				}
			}
		}
	}

	/**
	 * Transforms the given graph into an adjacency matrix representation.
	 *
	 * @param graph
	 * @return
	 */
	private double[][] getAdjacencyMatrix(final Graph graph) {
		final ArrayList<INode> containedNode = new ArrayList<>(graph.getNodes());
		final double[][] dist = new double[containedNode.size()][containedNode.size()];
		for (int i = 0; i < dist.length; i++) {
			for (int j = 0; j < dist.length; j++) {
				if (i == j) {
					dist[i][j] = 0.0;
				} else {
					dist[i][j] = Double.POSITIVE_INFINITY;
				}
			}
		}

		for (final IEdge edge : graph.getEdges()) {
			final INodeID nodeU = edge.fromId();
			final int u = getIndex(nodeU);
			final INodeID nodeV = edge.toId();
			final int v = getIndex(nodeV);

			// edge may have an edge length of 0, but the incident nodes must
			// not be identical and they need to have the
			// same position
			// assert edge.getLen() != 0.0 || (nodeU.getX() == nodeV.getX() &&
			// nodeU.getY() == nodeV.getY()
			// && !nodeU.getName().equals(nodeV.getName()));

			final double weight = getSpannerWeight(edge.getProperty(GenericGraphElementProperties.WEIGHT));

			dist[u][v] = weight;
			// dist[v][u] = weight; // edges are directed!
		}

		return dist;
	}


	/**
	 * Calculates the average and maximum spanner.
	 *
	 * Only the initialUnderlay and the resultUnderlay are considered, the
	 * initialOverlay and resultOverlay may be null.
	 */
	// N - 1 paths for targetPaths
	// N*(N-1)/2 paths for pairwise paths
	@Override
	public final void compute(final Graph initialUnderlay, final Graph resultUnderlay) {

		// important: the node set of initial graph and result graph must be the
		// same. otherwise, this won't work
		if (initialUnderlay.getNodeCount() != resultUnderlay.getNodeCount())
			throw new IllegalArgumentException("Node counts need to be equal!");

		initIndex(initialUnderlay);

		final int nodeCount = resultUnderlay.getNodeCount();

		final double[][] initialGraph = getAdjacencyMatrix(initialUnderlay);
		getAllPairShortestPaths(initialGraph);
		final double[][] resultGraph = getAdjacencyMatrix(resultUnderlay);
		getAllPairShortestPaths(resultGraph);

		// TODO MS: Here I removed the code for spanner to the base station, as
		// the base station is unknown to the new graph framework

		// final int targetIndex =
		// getIndex(initialOverlay.getBaseNode().getUnderlayNode());
		//
		// // to target
		// double sumSpanningValues = 0.0;
		// double maxSpanningValue = Double.MIN_VALUE;
		//
		// this.toTargetSpanningValues = new ArrayList<Double>(N - 1);
		// for (int i = 0; i < N; i++) {
		// if (i != targetIndex) {
		// // initial distance
		// final double initialDistance = initialGraph[i][targetIndex];
		//
		// // new distance
		// final double newDistance = resultGraph[i][targetIndex];
		//
		// // compute spanning value
		// double spanningValue;
		// if (newDistance == 0.0) { // this can really happen, if the
		// // nodes have equal positions
		// assert initialDistance == 0.0;
		//
		// spanningValue = 1.0;
		// } else {
		// spanningValue = newDistance / initialDistance;
		// }
		//
		// assert spanningValue >= 1.0;
		//
		// toTargetSpanningValues.add(spanningValue);
		//
		// sumSpanningValues += spanningValue;
		//
		// if (spanningValue > maxSpanningValue) {
		// maxSpanningValue = spanningValue;
		// }
		// }
		// }
		// this.valueMaxToBase = maxSpanningValue;
		// this.valueAverageToBase = sumSpanningValues / (N - 1);
		// assert toTargetSpanningValues.size() == N - 1;

		// pairwise: (n*(n -1))/2 values
		double sumSpanningValues = 0.0;
		double maxSpanningValue = Double.MIN_VALUE;
		this.pairwiseSpanningValues = new ArrayList<Double>();
		for (int i = 0; i < nodeCount - 1; i++) {
			for (int j = i + 1; j < nodeCount; j++) {
				// initial distance
				final double initialDistance = initialGraph[i][j];

				// new distance
				final double newDistance = resultGraph[i][j];

				// compute spanning value
				double spanningValue;
				if (newDistance == 0.0) { // this can really happen, if the
											// nodes have equal positions
					assert initialDistance == 0.0;

					spanningValue = 1.0;
				} else {
					spanningValue = newDistance / initialDistance;
				}

				//assert spanningValue >= 1.0;

				pairwiseSpanningValues.add(spanningValue);

				sumSpanningValues += spanningValue;

				if (spanningValue > maxSpanningValue) {
					maxSpanningValue = spanningValue;
				}
			}
		}
		this.valueMaxPairwise = maxSpanningValue;
		this.valueAveragePairwise = sumSpanningValues / ((nodeCount * (nodeCount - 1)) / 2);
		assert((nodeCount * (nodeCount - 1)) / 2) == pairwiseSpanningValues.size();
	}

	public double getAveragePairwiseSpanner() {
		return valueAveragePairwise;
	}

	public double getMaximumPairwiseSpanner() {
		return valueMaxPairwise;
	}

	@Override
	public void writeResults(final PropertyWriter resultWriter) {
		resultWriter.writeComment("Spanner properties: " + getClass().getName());
		resultWriter.writeComment("Note: spanner metrics do not contain the distance from nodes to themselves");

		resultWriter.writeProperty(getWeightMetricName() + "SpannerAveragePairwise", this.valueAveragePairwise);
		resultWriter.writeProperty(getWeightMetricName() + "SpannerMaxPairwise", this.valueMaxPairwise);
		// resultWriter.writeProperty(getWeightMetricName() +
		// "SpannerPairwiseValues", this.pairwiseSpanningValues);

		// This property cannot be calculated currently
		// resultWriter.writeProperty(getWeightMetricName() +
		// "SpannerAverageToBase", this.valueAverageToBase);
		// resultWriter.writeProperty(getWeightMetricName() +
		// "SpannerMaxToBase", this.valueMaxToBase);
		// resultWriter.writeProperty(getWeightMetricName() +
		// "SpannerToBaseValues", this.toTargetSpanningValues);
	}

	@Override
	public Iterable<JavaPlot> getPlots() {
		final List<JavaPlot> plots = new LinkedList<JavaPlot>();
		plots.add(DefaultPlots.getCfgPlot(this.pairwiseSpanningValues,
				this.getWeightMetricName() + ": Pairwise Spanning Values", this.getWeightMetricName(), "Probability"));
		plots.add(DefaultPlots.getCfgPlot(this.toTargetSpanningValues,
				this.getWeightMetricName() + ": ToTarget Spanning Values", this.getWeightMetricName(), "Probability"));
		return plots;
	}

	protected abstract double getSpannerWeight(double len);

	protected abstract String getWeightMetricName();
}
