package de.tudarmstadt.maki.simonstrator.api.component.topology;

/**
 * This event signals that a local topology has changed.
 *
 * Clients may query the topology provider and the changed topology (via its identifier) from the event.
 */
public class TopologyChangedEvent implements ITopologyChangedEvent {

	protected final TopologyProvider topologyProvider;
	protected final TopologyID topologyIdentifier;

	public TopologyChangedEvent(final TopologyProvider topologyProvider, final TopologyID topologyIdentifier) {
		this.topologyProvider = topologyProvider;
		this.topologyIdentifier = topologyIdentifier;
	}

	public TopologyProvider getTopologyProvider() {
		return topologyProvider;
	}

	public TopologyID getTopologyIdentifier() {
		return topologyIdentifier;
	}

	@Override
	public String toString() {
		return String.format("TopologyChangedEvent [provider=%s, ID=%s]", topologyProvider, topologyIdentifier);
	}

}
