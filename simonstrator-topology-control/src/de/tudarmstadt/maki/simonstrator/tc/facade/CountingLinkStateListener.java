package de.tudarmstadt.maki.simonstrator.tc.facade;

import java.util.HashMap;
import java.util.Map;

import de.tudarmstadt.maki.simonstrator.api.common.graph.IEdge;
import de.tudarmstadt.maki.simonstrator.tc.underlay.EdgeState;
import de.tudarmstadt.maki.simonstrator.tc.underlay.UnderlayTopologyProperties;

public class CountingLinkStateListener implements ILinkStateListener {
	private int activationCount = 0;
	private int inactivationCount = 0;
	private int unclassificationCount = 0;
	private final ITopologyControlFacade topologyControlFacade;

	protected Map<IEdge, EdgeState> linkStateModificationCache = new HashMap<>();

	public CountingLinkStateListener(final ITopologyControlFacade topologyControlFacade) {
		this.topologyControlFacade = topologyControlFacade;
      reset();
	}

	protected ITopologyControlFacade getTopologyControlFacade()
   {
      return topologyControlFacade;
   }

	public void clearLinkStateModificationCache() {
		linkStateModificationCache.clear();
	}

	public void reset() {
		activationCount = 0;
		inactivationCount = 0;
		unclassificationCount = 0;
		this.clearLinkStateModificationCache();
	}

	public int getActivationCount() {
		return activationCount;
	}

	public int getEffectiveActivationCount() {
		int effectiveActivationCount = 0;
		for (final IEdge edge : linkStateModificationCache.keySet()) {
			if (edge.getProperty(UnderlayTopologyProperties.EDGE_STATE) == EdgeState.ACTIVE)
				++effectiveActivationCount;
		}
		return effectiveActivationCount;
	}

	public int getInactivationCount() {
		return inactivationCount;
	}

	public int getEffectiveInactivationCount() {
		int effectiveInactivationCount = 0;
		for (final IEdge edge : linkStateModificationCache.keySet()) {
			if (edge.getProperty(UnderlayTopologyProperties.EDGE_STATE) == EdgeState.INACTIVE)
				++effectiveInactivationCount;
		}
		return effectiveInactivationCount;
	}

	public int getUnclassificationCount() {
		return unclassificationCount;
	}

	public int getEffectiveUnclassification() {
		int effectiveUnclassificationCount = 0;
		for (final IEdge edge : linkStateModificationCache.keySet()) {
			if (edge.getProperty(UnderlayTopologyProperties.EDGE_STATE) == EdgeState.UNCLASSIFIED)
				++effectiveUnclassificationCount;
		}
		return effectiveUnclassificationCount;
	}

	public int getAggregatedLinkStateChangeCount() {
		return this.activationCount + this.inactivationCount + this.unclassificationCount;
	}

	public int getEffectiveLinkStateChangeCount() {
		return this.linkStateModificationCache.size();
	}

	@Override
	public void linkActivated(final IEdge edge) {
		++activationCount;
		this.linkStateModificationCache.put(edge, EdgeState.ACTIVE);
	}

	@Override
	public void linkInactivated(final IEdge edge) {
		++inactivationCount;
		this.linkStateModificationCache.put(edge, EdgeState.INACTIVE);
	}

	@Override
	public void linkUnclassified(final IEdge edge) {
		++unclassificationCount;
		this.linkStateModificationCache.put(edge, EdgeState.UNCLASSIFIED);
	}

	public String format() {
		return String.format("[total count: a=%4d, i=%d, u=%4d, effective counts: a=%4d, i=%d, u=%4d]",
				getActivationCount(), getInactivationCount(), getUnclassificationCount(), getEffectiveActivationCount(),
				getEffectiveInactivationCount(), getEffectiveUnclassification());
	}

	@Override
	public String toString() {
		return String.format("Link state listener %s", format());
	}
}