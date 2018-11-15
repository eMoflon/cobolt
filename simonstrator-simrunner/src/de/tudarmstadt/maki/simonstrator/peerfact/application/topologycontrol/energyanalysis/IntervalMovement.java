package de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.energyanalysis;

import java.util.Set;

import de.tud.kom.p2psim.api.topology.movement.SimLocationActuator;
import de.tud.kom.p2psim.impl.topology.PositionVector;
import de.tud.kom.p2psim.impl.topology.movement.AbstractMovementModel;

public class IntervalMovement extends AbstractMovementModel {

	private int movementDimension = 0; // 0 : X, 1 : Y
	private double perStepDistanceInMeters = 20;

	public void setMovementDimension(final int movementDimension) {
		this.movementDimension = movementDimension;
	}

	public void setMovementStepSizeInMeters(final double movementStepSizeInMeters) {
		perStepDistanceInMeters = movementStepSizeInMeters;
	}

	@Override
	public void move() {

		final Set<SimLocationActuator> comps = getComponents();
		for (SimLocationActuator comp : comps) {
			final PositionVector pos = comp.getRealPosition();
			pos.setEntry(movementDimension, pos.getEntry(movementDimension) + perStepDistanceInMeters);
			updatePosition(comp, pos);
			// notifyPositionChange(comp);
		}
	}

}
