package de.tudarmstadt.maki.simonstrator.tc.component;

import de.tudarmstadt.maki.simonstrator.api.operation.PeriodicOperation;

/**
 * This operation controls the periodic execution of monitoring
 * 
 * @author Roland Kluge - Initial implementation
 */
class TopologyControlMonitoringOperation extends PeriodicOperation<TopologyControlComponent, Void> {

	/**
	 * Creates a periodic(!) operation with the given default interval (in min.)
	 * 
	 * @param topologyControlComponent
	 *            the corresponding component
	 */
	TopologyControlMonitoringOperation(final TopologyControlComponent topologyControlComponent) {
		super(topologyControlComponent, null, topologyControlComponent.getMonitoringInterval());
	}

	@Override
	protected void executeOnce() {
		this.getComponent().doMonitoring();
	}

	@Override
	protected long calculateNextInterval() {
		return this.getComponent().getMonitoringInterval();
	}

	@Override
	public Void getResult() {
		return null;
	}

}