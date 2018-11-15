package de.tudarmstadt.maki.simonstrator.tc.component;

import java.util.Map.Entry;

import de.tudarmstadt.maki.simonstrator.api.common.graph.IEdge;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INodeID;
import de.tudarmstadt.maki.simonstrator.api.component.topology.AdaptableTopologyProvider;
import de.tudarmstadt.maki.simonstrator.api.component.topology.TopologyID;
import de.tudarmstadt.maki.simonstrator.tc.facade.CountingLinkStateListener;
import de.tudarmstadt.maki.simonstrator.tc.facade.ITopologyControlFacade;
import de.tudarmstadt.maki.simonstrator.tc.underlay.EdgeState;
import de.tudarmstadt.maki.simonstrator.tc.underlay.UnderlayTopologyProperties;

/**
 * This listener propagates decisions of the TC algorithm to the logical link
 * layer
 *
 */
public class LogicalLinkLayerUpdatingLinkStateListener extends CountingLinkStateListener {

	private final SimpleTopologyProvider outputTopologyProvider;

	public LogicalLinkLayerUpdatingLinkStateListener(final SimpleTopologyProvider outputTopologyProvider,
			final ITopologyControlFacade topologyControlFacade) {
		super(topologyControlFacade);
		this.outputTopologyProvider = outputTopologyProvider;
	}

	public void applyCachedLinkStateModifications() {
		for (final Entry<IEdge, EdgeState> entry : linkStateModificationCache.entrySet()) {

			final IEdge edge = entry.getKey();
			final EdgeState state = entry.getValue();
			this.getTopologyControlFacade().updateEdgeAttribute(edge, UnderlayTopologyProperties.EDGE_STATE);

			switch (state) {
			case ACTIVE:
			case UNCLASSIFIED:
				activateLinkInVirtualTopology(edge);
				break;
			case INACTIVE:
				inactivateLinkInVirtualTopology(edge);
				break;
			}
		}
	}

	public void activateLinkInVirtualTopology(final IEdge edge) {
		final AdaptableTopologyProvider topologyComponentFrom = getTopologyComponent(edge.fromId());
		final AdaptableTopologyProvider topologyComponentTo = getTopologyComponent(edge.toId());
		final TopologyID topologyId = getOutputTopologyIdentifier();
		topologyComponentFrom.addNeighbor(topologyId, topologyComponentTo.getNode(topologyId));
	}

	public void inactivateLinkInVirtualTopology(final IEdge edge) {
		final AdaptableTopologyProvider topologyComponentFrom = getTopologyComponent(edge.fromId());
		final AdaptableTopologyProvider topologyComponentTo = getTopologyComponent(edge.toId());
		final TopologyID topologyId = getOutputTopologyIdentifier();
		topologyComponentFrom.removeNeighbor(topologyId, topologyComponentTo.getNode(topologyId));
	}

	private TopologyID getOutputTopologyIdentifier() {
		return this.outputTopologyProvider.getTopologyID();
	}

	private AdaptableTopologyProvider getTopologyComponent(final INodeID nodeId) {
		return this.outputTopologyProvider.getTopologyComponent(nodeId);
	}

}