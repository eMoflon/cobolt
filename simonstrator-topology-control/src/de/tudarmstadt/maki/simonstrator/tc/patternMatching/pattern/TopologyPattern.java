package de.tudarmstadt.maki.simonstrator.tc.patternMatching.pattern;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import de.tudarmstadt.maki.simonstrator.api.common.graph.EdgeID;
import de.tudarmstadt.maki.simonstrator.api.common.graph.Graph;
import de.tudarmstadt.maki.simonstrator.api.common.graph.IEdge;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INode;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INodeID;
import de.tudarmstadt.maki.simonstrator.api.common.graph.Node;
import de.tudarmstadt.maki.simonstrator.tc.patternMatching.constraint.GraphElementConstraint;

public class TopologyPattern implements Serializable {

	private static final long serialVersionUID = 7601747710688053872L;

	private final INodeID origin;

	private final Graph graph;

	private final Collection<GraphElementConstraint> constraints;

	private final Collection<TopologyPattern> nacPatterns;

	public TopologyPattern(final INodeID origin, final Graph graph, final Collection<GraphElementConstraint> constraints,
			final Collection<TopologyPattern> nacPatterns) {
		this.origin = origin;
		this.graph = graph;
		this.constraints = constraints;
		this.nacPatterns = nacPatterns;
	}

	public TopologyPattern(final INodeID origin, final Graph graph) {
		this(origin, graph, new ArrayList<GraphElementConstraint>(), new ArrayList<TopologyPattern>());
	}

	public TopologyPattern(final INodeID origin, final Graph graph, final Collection<GraphElementConstraint> constraints) {
		this(origin, graph, constraints, new ArrayList<TopologyPattern>());
	}

	/**
	 * Returns the number of hops that is necessary to reach all nodes in the pattern from the origina of the pattern.
	 * @return
	 */
	public int calculateHorizon() {
		// Calculation is implemented as naive BFS search
		final Queue<INodeID> bfsQueue = new LinkedList<>();
		final Map<INodeID, Integer> depth = new HashMap<>();
		int maxDepth = 0;
		bfsQueue.add(origin);
		depth.put(origin, 0);
		while (bfsQueue.isEmpty()) {
			final INodeID node = bfsQueue.poll();
			final int nextLevelDepth = depth.get(node) + 1;
			final Set<INodeID> neighbors = graph.getNeighbors(node);
			if (!neighbors.isEmpty()) {
				maxDepth = Math.max(maxDepth, nextLevelDepth);
			}

			for (final INodeID neighbor : neighbors) {
				if (!depth.containsKey(neighbor)) {
					depth.put(neighbor, nextLevelDepth);
				} else {
					depth.put(neighbor, Math.min(depth.get(neighbor), nextLevelDepth));
				}
				bfsQueue.add(neighbor);
			}
		}

		return maxDepth;
	}

	public INodeID getOrigin() {
		return origin;
	}

	public Collection<GraphElementConstraint> getConstraints() {
		return constraints;
	}

	public void addConstraint(final GraphElementConstraint constraint) {
		this.constraints.add(constraint);
	}

	public Set<INodeID> getNeighbors(final Node node) {
		return this.graph.getNeighbors(node);
	}

	public Iterable<INodeID> getNodeVariables() {
		return this.graph.getNodeIds();
	}

	public Set<? extends IEdge> getLinkVariables() {
		return this.graph.getEdges();
	}

	public Graph getGraph() {
		return this.graph;
	}

	public Iterable<INodeID> getVariables() {
		return this.graph.getNodeIds();
	}

	public Collection<TopologyPattern> getNegativeApplicationConstraints() {
		return this.nacPatterns;
	}

	@Override
	public String toString() {
		return String.format("Pattern [graph: [%s], constraints: [%s], NACs: [%s]]", this.graph, StringUtils.join(this.constraints, ", "),
				StringUtils.join(this.nacPatterns, ",\n"));
	}

	public INode getNodeVariableById(final String string) {
		return this.graph.getNode(INodeID.get(string));
	}

	public IEdge getLinkVariableById(final String string) {
		return this.graph.getEdge(EdgeID.get(string));
	}

}
