package de.tudarmstadt.maki.simonstrator.tc.facade;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.tudarmstadt.maki.simonstrator.api.Graphs;
import de.tudarmstadt.maki.simonstrator.api.common.graph.Graph;
import de.tudarmstadt.maki.simonstrator.api.common.graph.GraphElementProperties;
import de.tudarmstadt.maki.simonstrator.api.common.graph.IEdge;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INode;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INodeID;
import de.tudarmstadt.maki.simonstrator.api.component.sis.type.SiSType;
import de.tudarmstadt.maki.simonstrator.tc.component.TopologyControlComponent;
import de.tudarmstadt.maki.simonstrator.tc.filtering.EdgeFilter;
import de.tudarmstadt.maki.simonstrator.tc.underlay.EdgeState;
import de.tudarmstadt.maki.simonstrator.tc.underlay.UnderlayTopologyProperties;

/**
 * Convenient implementation base class for all implementations of
 * {@link ITopologyControlFacade}.
 *
 * @author Roland Kluge - Initial Implementation
 */
public abstract class TopologyControlFacade_ImplBase implements ITopologyControlFacade {

	protected final Graph simonstratorGraph;

	protected final List<ILinkStateListener> linkStateListeners;

	protected final List<IContextEventListener> contextEventListeners;

	protected TopologyControlOperationMode operationMode;

	protected int localViewSize;

	private boolean areContextEventListenersMuted;

	private boolean areLinkStateListenersMuted;

	private Set<EdgeFilter> edgeFilters;

	private TopologyControlAlgorithmID algorithmId;

   private TopologyControlComponent topologyControlComponent;

	public TopologyControlFacade_ImplBase() {
		this.simonstratorGraph = Graphs.createGraph();
		this.operationMode = TopologyControlOperationMode.NOT_SET;
		this.linkStateListeners = new LinkedList<>();
		this.contextEventListeners = new LinkedList<>();
		this.edgeFilters = new HashSet<>();
	}

	@Override
	public void configureAlgorithm(TopologyControlAlgorithmID algorithmID) {
		this.algorithmId = algorithmID;
	}

	@Override
	public TopologyControlAlgorithmID getConfiguredAlgorithm() {
		return this.algorithmId;
	}

	@Override
	public void run() {
		this.run(new TopologyControlAlgorithmParamters());
	}

	@Override
	public Graph getGraph() {
		return this.simonstratorGraph;
	}

	@Override
	public INode addNode(INode prototype) {
		final INode simNode = Graphs.createNode(prototype.getId());
		simNode.addPropertiesFrom(prototype);
		this.simonstratorGraph.addNode(simNode);
		return simNode;
	}

	@Override
	public IEdge addEdge(IEdge prototype) {
		final IEdge edge = Graphs.createDirectedEdge(prototype.getId(), prototype.fromId(), prototype.toId());
		edge.addPropertiesFrom(prototype);
		this.simonstratorGraph.addEdge(edge);
		return edge;
	}

	@Override
	public <T> void updateNodeAttribute(INode node, SiSType<T> property) {
		this.getGraph().getNode(node.getId()).setProperty(property, node.getProperty(property));
	}

	@Override
	public <T> void updateEdgeAttribute(IEdge edge, SiSType<T> property) {
		this.getGraph().getEdge(edge.getId()).setProperty(property, edge.getProperty(property));
	}

	@Override
	public void removeEdge(IEdge edge) {
		this.simonstratorGraph.removeEdge(edge);
	}

	@Override
	public void removeNode(INodeID node) {
		this.simonstratorGraph.removeNode(node);
	}

	@Override
	public Collection<ILinkStateListener> getLinkStateListeners() {
		return Collections.unmodifiableList(this.linkStateListeners);
	}

	@Override
	public void addLinkStateListener(ILinkStateListener listener) {
		this.linkStateListeners.add(listener);
	}

	@Override
	public void removeLinkStateListener(ILinkStateListener listener) {
		this.linkStateListeners.remove(listener);
	}

	@Override
	public Collection<IContextEventListener> getContextEventListeners() {
		return Collections.unmodifiableList(this.contextEventListeners);
	}

	@Override
	public void addContextEventListener(final IContextEventListener listener) {
		this.contextEventListeners.add(listener);
	}

	@Override
	public void removeContextEventListener(final IContextEventListener listener) {
		this.contextEventListeners.remove(listener);
	}

	@Override
	public void beginContextEventSequence() {
		// nop
	}

	@Override
	public void endContextEventSequence() {
		// nop
	}

	@Override
	public void endTopologyControlSequence() {
		// nop
	}

	@Override
	public void checkConstraintsAfterTopologyControl() {
		// nop
	}

	@Override
	public void checkConstraintsAfterContextEvent() {
		// nop
	}

	@Override
	public void resetConstraintViolationCounter() {
	}

	@Override
	public int getConstraintViolationCount() {
		return 0;
	}

	@Override
	public void setContextEventListenersMuted(boolean muted) {
		this.areContextEventListenersMuted = muted;
	}

	@Override
	public void setLinkStateModificationListenersMuted(boolean muted) {
		this.areLinkStateListenersMuted = true;
	}

	@Override
	public boolean areContextEventListenersMuted() {
		return this.areContextEventListenersMuted;
	}

	@Override
	public boolean areLinkStateModificationListenersMuted() {
		return this.areLinkStateListenersMuted;
	}

	protected void firePostNodeAdded(INode simNode) {
		if (this.areContextEventListenersMuted)
			return;

		for (final IContextEventListener contextEventListener : this.contextEventListeners) {
			contextEventListener.postNodeAdded(simNode);
		}
	}

	protected void firePreRemovedNode(final INode removedNodeId) {
		if (this.areContextEventListenersMuted)
			return;

		for (final IContextEventListener contextEventListener : this.contextEventListeners) {
			contextEventListener.preNodeRemoved(removedNodeId);
		}
	}

	protected <T> void firePostNodeAttributeUpdated(final INode simNode, final SiSType<T> property) {
		if (this.areContextEventListenersMuted)
			return;

		for (final IContextEventListener contextEventListener : this.contextEventListeners) {
			contextEventListener.postNodeAttributeUpdated(simNode, property);
		}
	}

	protected void firePreEdgeRemoved(final IEdge reverseSimEdge) {
		if (this.areContextEventListenersMuted)
			return;

		for (final IContextEventListener contextEventListener : this.contextEventListeners) {
			contextEventListener.preEdgeRemoved(reverseSimEdge);
		}
	}

	protected <T> void firePostEdgeAttributeUpdated(final IEdge simEdge, final SiSType<T> property) {
		if (this.areContextEventListenersMuted)
			return;

		for (final IContextEventListener contextEventListener : this.contextEventListeners) {
			contextEventListener.postEdgeAttributeUpdated(simEdge, property);
		}
	}

	protected void firePostEdgeAdded(final IEdge edge) {
		if (this.areContextEventListenersMuted)
			return;

		for (final IContextEventListener contextEventListener : this.contextEventListeners) {
			contextEventListener.postEdgeAdded(edge);
		}
	}

	protected void fireLinkStateChanged(final IEdge edge) {
		if (this.areLinkStateListenersMuted)
			return;

		GraphElementProperties.validateThatPropertyIsPresent(edge, UnderlayTopologyProperties.EDGE_STATE);

		for (ILinkStateListener listener : this.linkStateListeners) {
			switch (edge.getProperty(UnderlayTopologyProperties.EDGE_STATE)) {
			case ACTIVE:
				listener.linkActivated(edge);
				break;
			case INACTIVE:
				listener.linkInactivated(edge);
				break;
			case UNCLASSIFIED:
				listener.linkUnclassified(edge);
				break;
			default:
			}
		}
	}

	@Override
	public void connectOppositeEdges(IEdge fwdEdgePrototype, IEdge bwdEdgePrototype) {
		final IEdge fwdEdge = this.simonstratorGraph.getEdge(fwdEdgePrototype.getId());
		if (fwdEdge == null)
			throw new IllegalArgumentException(
					String.format("No edge with ID '%s' exists (yet).", fwdEdgePrototype.getId()));
		final IEdge bwdEdge = this.simonstratorGraph.getEdge(bwdEdgePrototype.getId());
		if (bwdEdge == null)
			throw new IllegalArgumentException(
					String.format("No edge with ID '%s' exists (yet).", bwdEdgePrototype.getId()));
		this.simonstratorGraph.makeInverseEdges(fwdEdge, bwdEdge);
	}

	@Override
	public void unclassifyAllLinks() {
		for (final IEdge edge : this.simonstratorGraph.getEdges()) {
			edge.setProperty(UnderlayTopologyProperties.EDGE_STATE, EdgeState.UNCLASSIFIED);
		}
	}

	/**
	 * Always true
	 */
	@Override
	public boolean supportsOperationMode(TopologyControlAlgorithmID algorithmId, TopologyControlOperationMode mode) {
		return true;
	}

	@Override
	public void setOperationMode(TopologyControlOperationMode mode) {
		this.operationMode = mode;
	}

	@Override
	public void setLocalViewSize(final int localViewSize) {
		this.localViewSize = localViewSize;
	}

	@Override
	public void addEdgeFilter(EdgeFilter filter) {
		this.edgeFilters.add(filter);
	}

	@Override
	public void removeEdgeFilter(EdgeFilter filter) {
		this.edgeFilters.remove(filter);
	}

	@Override
	public void clearEdgeFilters() {
		this.edgeFilters.clear();
	}

	@Override
	public Collection<EdgeFilter> getEdgeFilters() {
		return Collections.unmodifiableCollection(this.edgeFilters);
	}

	@Override
	public void initalize() {
	}

	@Override
	public void shutdown() {
	}

	@Override
	public void setTopologyControlComponent(final TopologyControlComponent topologyControlComponent)
	{
      this.topologyControlComponent = topologyControlComponent;
	}

	public TopologyControlComponent getTopologyControlComponent()
   {
      return topologyControlComponent;
   }
}
