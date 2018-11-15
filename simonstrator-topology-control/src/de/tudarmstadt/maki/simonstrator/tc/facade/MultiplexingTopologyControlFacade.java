package de.tudarmstadt.maki.simonstrator.tc.facade;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.tudarmstadt.maki.simonstrator.api.common.graph.Graph;
import de.tudarmstadt.maki.simonstrator.api.common.graph.IEdge;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INode;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INodeID;
import de.tudarmstadt.maki.simonstrator.api.component.sis.type.SiSType;
import de.tudarmstadt.maki.simonstrator.tc.component.TopologyControlComponent;
import de.tudarmstadt.maki.simonstrator.tc.filtering.EdgeFilter;

public class MultiplexingTopologyControlFacade implements ITopologyControlFacade {

	private final List<ITopologyControlFacade> delegateFacades = new ArrayList<>();

	public void addDelegateFacade(final ITopologyControlFacade facade) {
		this.delegateFacades.add(facade);
	}

	public void removeDelegateFacade(final ITopologyControlFacade facade) {
		this.delegateFacades.remove(facade);
	}

	public void clearDelegateFacades() {
		this.delegateFacades.clear();
	}

	@Override
	public void configureAlgorithm(TopologyControlAlgorithmID algorithmID) {
		for (final ITopologyControlFacade facade : this.delegateFacades) {
			facade.configureAlgorithm(algorithmID);
		}
	}

	@Override
	public TopologyControlAlgorithmID getConfiguredAlgorithm() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void run() {
		this.run(new TopologyControlAlgorithmParamters());
	}

	@Override
	public void run(TopologyControlAlgorithmParamters parameters) {
		for (final ITopologyControlFacade facade : this.delegateFacades) {
			facade.run(parameters);
		}
	}

	/**
	 * Not supported
	 */
	@Override
	public Graph getGraph() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Returns null
	 */
	@Override
	public INode addNode(INode prototype) {
		for (final ITopologyControlFacade facade : this.delegateFacades) {
			facade.addNode(prototype);
		}
		return null;
	}

	/**
	 * Returns null
	 */
	@Override
	public IEdge addEdge(IEdge prototype) {
		for (final ITopologyControlFacade facade : this.delegateFacades) {
			facade.addEdge(prototype);
		}
		return null;
	}

	@Override
	public <T> void updateNodeAttribute(INode node, SiSType<T> property) {
		for (final ITopologyControlFacade facade : this.delegateFacades) {
			facade.updateNodeAttribute(node, property);
		}
	}

	@Override
	public <T> void updateEdgeAttribute(IEdge edge, SiSType<T> property) {
		for (final ITopologyControlFacade facade : this.delegateFacades) {
			facade.updateEdgeAttribute(edge, property);
		}
	}

	@Override
	public void removeEdge(IEdge element) {
		this.delegateFacades.forEach(facade -> facade.removeEdge(element));
	}

	@Override
	public void removeNode(INodeID node) {
		this.delegateFacades.forEach(facade -> facade.removeNode(node));
	}

	@Override
	public Collection<ILinkStateListener> getLinkStateListeners() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addLinkStateListener(ILinkStateListener listener) {
		this.delegateFacades.forEach(facade -> facade.addLinkStateListener(listener));
	}

	@Override
	public void removeLinkStateListener(ILinkStateListener listener) {
		this.delegateFacades.forEach(facade -> facade.removeLinkStateListener(listener));
	}

	@Override
	public Collection<IContextEventListener> getContextEventListeners() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addContextEventListener(IContextEventListener listener) {
		this.delegateFacades.forEach(facade -> facade.addContextEventListener(listener));
	}

	@Override
	public void removeContextEventListener(IContextEventListener listener) {
		this.delegateFacades.forEach(facade -> facade.removeContextEventListener(listener));
	}

	@Override
	public void beginContextEventSequence() {
		this.delegateFacades.forEach(ITopologyControlFacade::beginContextEventSequence);
	}

	@Override
	public void endContextEventSequence() {
		this.delegateFacades.forEach(ITopologyControlFacade::endContextEventSequence);
	}

	@Override
	public void endTopologyControlSequence() {
		this.delegateFacades.forEach(ITopologyControlFacade::endTopologyControlSequence);
	}

	@Override
	public void resetConstraintViolationCounter() {
		this.delegateFacades.forEach(ITopologyControlFacade::resetConstraintViolationCounter);
	}

	/**
	 * Returns the sum of the individual counters
	 *
	 * @return
	 */
	@Override
	public int getConstraintViolationCount() {
		int sum = 0;
		for (final ITopologyControlFacade facade : this.delegateFacades) {
			sum += facade.getConstraintViolationCount();
		}
		return sum;
	}

	@Override
	public void checkConstraintsAfterTopologyControl() {
		this.delegateFacades.forEach(ITopologyControlFacade::checkConstraintsAfterTopologyControl);
	}

	@Override
	public void checkConstraintsAfterContextEvent() {
		this.delegateFacades.forEach(ITopologyControlFacade::checkConstraintsAfterContextEvent);
	}

	@Override
	public void setContextEventListenersMuted(boolean muted) {
		this.delegateFacades.forEach(facade -> facade.setContextEventListenersMuted(muted));
	}

	/**
	 * Returns whether all CE listeners are muted
	 */
	@Override
	public boolean areContextEventListenersMuted() {
		for (final ITopologyControlFacade facade : this.delegateFacades) {
			if (!facade.areContextEventListenersMuted())
				return false;
		}
		return true;
	}

	@Override
	public void setLinkStateModificationListenersMuted(boolean muted) {
		this.delegateFacades.forEach(facade -> facade.setLinkStateModificationListenersMuted(muted));
	}

	/**
	 * Returns whether all LSM listeners are muted
	 */
	@Override
	public boolean areLinkStateModificationListenersMuted() {
		for (final ITopologyControlFacade facade : this.delegateFacades) {
			if (!facade.areLinkStateModificationListenersMuted())
				return false;
		}
		return true;
	}

	@Override
	public void connectOppositeEdges(IEdge fwdEdge, IEdge bwdEdge) {
		this.delegateFacades.forEach(facade -> facade.connectOppositeEdges(fwdEdge, bwdEdge));
	}

	@Override
	public void unclassifyAllLinks() {
		this.delegateFacades.forEach(ITopologyControlFacade::unclassifyAllLinks);
	}

	/**
	 * Returns true iff all registered facades support the given mode
	 */
	@Override
	public boolean supportsOperationMode(TopologyControlAlgorithmID algorithmId,
			final TopologyControlOperationMode mode) {
		return this.delegateFacades.stream().allMatch(facade -> facade.supportsOperationMode(algorithmId, mode));
	}

	@Override
	public void setOperationMode(final TopologyControlOperationMode mode) {
		this.delegateFacades.forEach(facade -> setOperationMode(mode));
	}

	@Override
	public void setLocalViewSize(int localViewSize) {
		this.delegateFacades.forEach(facade -> setLocalViewSize(localViewSize));
	}

	@Override
	public void addEdgeFilter(EdgeFilter filter) {
		this.delegateFacades.forEach(facade -> facade.addEdgeFilter(filter));
	}

	@Override
	public void removeEdgeFilter(EdgeFilter filter) {
		this.delegateFacades.forEach(facade -> facade.removeEdgeFilter(filter));
	}

	@Override
	public void clearEdgeFilters() {
		this.delegateFacades.forEach(ITopologyControlFacade::clearEdgeFilters);
	}

	/**
	 * Not supported
	 */
	@Override
	public Collection<EdgeFilter> getEdgeFilters() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void shutdown() {
		this.delegateFacades.forEach(ITopologyControlFacade::shutdown);
	}

	@Override
	public void initalize() {
		this.delegateFacades.forEach(ITopologyControlFacade::initalize);
	}

	@Override
	public void setTopologyControlComponent(final TopologyControlComponent topologyControlComponent)
	{
	   this.delegateFacades.forEach(facade -> facade.setTopologyControlComponent(topologyControlComponent));
	}

}
