package de.tudarmstadt.maki.simonstrator.tc.component;

import de.tudarmstadt.maki.simonstrator.api.operation.PeriodicOperation;

/**
 * This operation controls the periodic execution of topology control
 * 
 * @author Roland Kluge - Initial implementation
 */
class TopologyControlPeriodicOperation extends PeriodicOperation<TopologyControlComponent, Void> {

	/**
	 * Creates a periodic(!) operation with the given default interval (in min.)
	 * 
	 * @param topologyControlComponent
	 *            the corresponding component
	 * @param intervalInMinutes
	 *            the Topology Control interval in minutes
	 */
	TopologyControlPeriodicOperation(final TopologyControlComponent topologyControlComponent) {
		super(topologyControlComponent, null, topologyControlComponent.getTopologyControlInterval());
	}

	/**
	 * Invokes
	 * {@link TopologyControlComponent#executeTopologyControlIteration()} and
	 * reschedules this operation based on the configured interval
	 */
	@Override
	protected void executeOnce() {
		this.getComponent().executeTopologyControlIteration();
	}

	@Override
	protected long calculateNextInterval() {
		return this.getComponent().getTopologyControlInterval();
	}

	@Override
	public Void getResult() {
		return null;
	}

}