/*
 * Copyright (c) 2005-2011 KOM - Multimedia Communications Lab
 *
 * This file is part of PeerfactSim.KOM.
 * 
 * PeerfactSim.KOM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 * 
 * PeerfactSim.KOM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with PeerfactSim.KOM.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package de.tud.kom.p2psim.impl.topology.movement;

import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;

import de.tud.kom.p2psim.api.topology.Topology;
import de.tud.kom.p2psim.api.topology.movement.MovementModel;
import de.tud.kom.p2psim.api.topology.movement.SimLocationActuator;
import de.tud.kom.p2psim.api.topology.placement.PlacementModel;
import de.tud.kom.p2psim.impl.topology.PositionVector;
import de.tud.kom.p2psim.impl.topology.TopologyFactory;
import de.tudarmstadt.maki.simonstrator.api.Binder;
import de.tudarmstadt.maki.simonstrator.api.Event;
import de.tudarmstadt.maki.simonstrator.api.EventHandler;
import de.tudarmstadt.maki.simonstrator.api.Randoms;
import de.tudarmstadt.maki.simonstrator.api.Time;

/**
 * Unified movement Models. Can be used inside an Application (virtual Position)
 * or Device (physical Position) or anything else that implements
 * MovementSupported. They support automatic triggering using an Operation or
 * you may trigger them directly from within your Application by calling move()
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 04/25/2011
 */
public abstract class AbstractMovementModel implements MovementModel {

	private Set<SimLocationActuator> components = new LinkedHashSet<SimLocationActuator>();

	protected PositionVector worldDimensions;

	private long timeBetweenMoveOperations = -1;

	private Random random = Randoms.getRandom(AbstractMovementModel.class);

	/**
	 * This default implementation relies on {@link PlacementModel}s to be
	 * configured in the {@link TopologyFactory}
	 */
	@Override
	public void placeComponent(SimLocationActuator actuator) {
		// not supported
	}
	
	@Override
	public void changeTargetLocation(SimLocationActuator actuator,
			double longitude, double latitude) throws UnsupportedOperationException {
		// not supported by default. Extend this method, if needed.
		throw new UnsupportedOperationException();
	}

	/**
	 * Gets called periodically (after timeBetweenMoveOperations) or by an
	 * application and should be used to recalculate positions
	 */
	public abstract void move();

	/**
	 * Get all participating Components
	 * 
	 * @return
	 */
	protected Set<SimLocationActuator> getComponents() {
		return components;
	}

	/**
	 * Add a component to this movement-Model (used to keep global information
	 * about all participants in a movement model). Each Component acts as a
	 * callback upon movement of this component
	 * 
	 * @param component
	 */
	@Override
	public void addComponent(SimLocationActuator component) {
		if (worldDimensions == null) {
			worldDimensions = Binder.getComponentOrNull(Topology.class)
					.getWorldDimensions();
		}
		components.add(component);
	}

	/**
	 * Get a valid delta-Vector (does not cross world-boundaries and does not
	 * exceed moveSpeedLimit)
	 * 
	 * @param oldPosition
	 * @param minSpeed
	 * @param maxSpeed
	 * @return
	 */
	protected PositionVector getRandomDeltaWithinSpeed(
			PositionVector oldPosition, double minSpeed, double maxSpeed) {
		return getRandomDelta(oldPosition,
				minSpeed * getTimeBetweenMoveOperations() / Time.SECOND,
				maxSpeed * getTimeBetweenMoveOperations() / Time.SECOND);
	}

	/**
	 * Get a valid delta-Vector, where each abs(vector) must not exceed
	 * maxLength
	 * 
	 * @param oldPosition
	 * @param minLength
	 * @param maxLength
	 * @return
	 */
	protected PositionVector getRandomDelta(PositionVector oldPosition,
			double minLength, double maxLength) {
		assert minLength <= maxLength;
		PositionVector delta = null;

		if (oldPosition.getDimensions() == 2) {
			double angle = random.nextDouble() * 2 * Math.PI;
			double length = random.nextDouble() * (maxLength - minLength)
					+ minLength;
			double vectorX = -length * Math.sin(angle);
			double vectorY = length * Math.cos(angle);
			delta = new PositionVector(vectorX, vectorY);
		} else {
			throw new AssertionError(
					"Rotation for R3 and larger are not yet implemented. Feel free to do so ;)");
		}

		assert delta.getLength() >= minLength && delta.getLength() <= maxLength;

		return delta;
	}

	/**
	 * Returns if this is a valid position within the boundaries
	 * 
	 * @return
	 */
	protected boolean isValidPosition(PositionVector vector) {
		for (int i = 0; i < vector.getDimensions(); i++) {
			if (vector.getEntry(i) > getWorldDimension(i)
					|| vector.getEntry(i) < 0) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Call this method to finally update the location of the given component.
	 * 
	 * @param actuator
	 * @param newPosition
	 */
	protected void updatePosition(SimLocationActuator actuator,
			PositionVector newPosition) {
		this.updatePosition(actuator, newPosition.getLongitude(),
				newPosition.getLatitude());
	}

	/**
	 * Call this method to finally update the location of the given component.
	 * 
	 * @param actuator
	 * @param newPosition
	 */
	protected void updatePosition(SimLocationActuator actuator,
			double longitude, double latitude) {
		actuator.updateCurrentLocation(longitude, latitude);
	}

	/**
	 * Returns a random int between from and to, including both interval ends!
	 * 
	 * @param from
	 * @param to
	 * @return
	 */
	protected int getRandomInt(int from, int to) {
		int intervalSize = Math.abs(to - from);
		return (random.nextInt(intervalSize + 1) + from);
	}

	protected double getRandomDouble(double from, double to) {
		double intervalSize = Math.abs(to - from);
		return (random.nextDouble() * intervalSize + from);
	}

	/**
	 * Move models can periodically calculate new positions and notify
	 * listeners, if a position changed. Here you can specify the interval
	 * between these notifications/calculations. If this is set to zero, there
	 * is no periodical execution, which may be useful if you want to call
	 * move() from within an application.
	 * 
	 * @param timeBetweenMoveOperations
	 */
	public void setTimeBetweenMoveOperations(long timeBetweenMoveOperations) {
		if (timeBetweenMoveOperations > 0) {
			this.timeBetweenMoveOperations = timeBetweenMoveOperations;
			assert timeBetweenMoveOperations > 0;
			reschedule();
		}
	}

	protected void reschedule() {
		Event.scheduleWithDelay(timeBetweenMoveOperations, new PeriodicMove(),
				null, 0);
	}

	/**
	 * The time in Simulation units between move operations
	 * 
	 * @return
	 */
	public long getTimeBetweenMoveOperations() {
		return timeBetweenMoveOperations;
	}

	public double getWorldDimension(int dim) {
		return worldDimensions.getEntry(dim);
	}

	/**
	 * Triggers the periodic move
	 * 
	 * @author Bjoern Richerzhagen
	 * @version 1.0, 20.03.2012
	 */
	protected class PeriodicMove implements EventHandler {

		@Override
		public void eventOccurred(Object content, int type) {
			move();
			reschedule();
		}

	}

}
