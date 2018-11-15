package de.tudarmstadt.maki.simonstrator.tc.graph.algorithm;

import java.util.ArrayList;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.math3.stat.StatUtils;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import de.tudarmstadt.maki.simonstrator.api.common.graph.GenericGraphElementProperties;
import de.tudarmstadt.maki.simonstrator.api.common.graph.Graph;
import de.tudarmstadt.maki.simonstrator.api.common.graph.GraphElementProperties;
import de.tudarmstadt.maki.simonstrator.api.common.graph.GraphElementProperty;
import de.tudarmstadt.maki.simonstrator.api.common.graph.IEdge;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INode;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INodeID;

// Floyd-Warshall:
// http://en.wikipedia.org/wiki/Floyd%E2%80%93Warshall_algorithm
// distance/adjacency matrix provided:
// dis is initialized with edge weight -> dis[u][v] = edge weight of edge
// (u,v), Double.INFINITY if it doesn't exist, dist[v][v] = 0
public class FloydWarshallAlgorithm {

	private GraphElementProperty<Double> property = GenericGraphElementProperties.HOP_COUNT;

	final BiMap<INodeID, Integer> nodeToIndex;
	final BiMap<Integer, INodeID> indexToNode;
	double[][] lastResult = null;

	public FloydWarshallAlgorithm() {
		this.nodeToIndex = HashBiMap.create();
		this.indexToNode = this.nodeToIndex.inverse();
	}

	/**
	 * The property that is used to determine the 'weight' of an edge.
	 * 
	 * @param property
	 */
	public void setProperty(GraphElementProperty<Double> property) {
		this.property = property;
	}

	public double[][] compute(final Graph graph) {
		initIndex(graph);
		final double[][] dist = getAdjacencyMatrix(graph);
		for (int k = 0; k < dist.length; k++) {
			for (int i = 0; i < dist.length; i++) {
				for (int j = 0; j < dist.length; j++) {
					if (dist[i][j] > (dist[i][k] + dist[k][j])) {
						dist[i][j] = dist[i][k] + dist[k][j];
					}
				}
			}
		}

		lastResult = dist;
		return dist;
	}

	public double getMaximumDistance() {
		double maximum = Double.MIN_VALUE;
		for (final double[] row : lastResult) {
			maximum = Math.max(maximum, NumberUtils.max(row));
		}
		return maximum;
	}

	public double getMinimumDistance() {
		double minimum = Double.MAX_VALUE;
		for (final double[] row : lastResult) {
			minimum = Math.min(minimum, NumberUtils.min(row));
		}
		return minimum;
	}

	public double getAverageDistance() {
		double sum = 0.0;
		for (final double[] row : lastResult) {
			sum += StatUtils.sum(row);
		}

		final int nodeCount = lastResult.length;
		final int allEdgesCount = nodeCount * (nodeCount - 1) / 2;
		sum /= allEdgesCount;

		return sum;
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

			final double weight = determineWeight(edge);

			dist[u][v] = weight;
		}

		return dist;
	}

	private Double determineWeight(final IEdge edge) {

		if (GenericGraphElementProperties.HOP_COUNT == this.property)
			return 1.0;
		else {
			GraphElementProperties.validateThatPropertyIsPresent(edge, property);
			return edge.getProperty(property);
		}
	}

	private void addToIndex(INodeID node, int index) {
		nodeToIndex.put(node, index);
	}

	public int getIndex(INodeID nodeId) {
		return nodeToIndex.get(nodeId);
	}

	private void initIndex(Graph graph) {
		nodeToIndex.clear();

		int index = 0;
		for (INode node : graph.getNodes()) {
			addToIndex(node.getId(), index++);
		}
	}
}
