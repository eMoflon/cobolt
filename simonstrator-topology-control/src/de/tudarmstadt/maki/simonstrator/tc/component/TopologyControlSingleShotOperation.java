package de.tudarmstadt.maki.simonstrator.tc.component;

import de.tudarmstadt.maki.simonstrator.api.operation.AbstractOperation;

/**
 * This operation controls the single-shot execution of topology control
 * 
 * @author Roland Kluge - Initial implementation
 *
 */
public class TopologyControlSingleShotOperation extends AbstractOperation<TopologyControlComponent, Void> {

	protected TopologyControlSingleShotOperation(final TopologyControlComponent topologyControlComponent) {
		super(topologyControlComponent);
	}

	@Override
	protected void execute() {
		this.getComponent().executeTopologyControlIteration();
	}

	@Override
	public Void getResult() {
		return null;
	}

}