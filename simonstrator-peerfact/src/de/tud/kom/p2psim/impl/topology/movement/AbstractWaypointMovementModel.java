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
import java.util.WeakHashMap;

import de.tud.kom.p2psim.api.topology.movement.MovementModel;
import de.tud.kom.p2psim.api.topology.movement.SimLocationActuator;
import de.tud.kom.p2psim.api.topology.movement.local.LocalMovementStrategy;
import de.tud.kom.p2psim.api.topology.placement.PlacementModel;
import de.tud.kom.p2psim.api.topology.waypoints.WaypointModel;
import de.tud.kom.p2psim.impl.scenario.simcfg2.annotations.After;
import de.tud.kom.p2psim.impl.scenario.simcfg2.annotations.Configure;
import de.tud.kom.p2psim.impl.topology.PositionVector;
import de.tud.kom.p2psim.impl.topology.TopologyFactory;
import de.tud.kom.p2psim.impl.util.Either;
import de.tud.kom.p2psim.impl.util.geo.maps.MapLoader;
import de.tudarmstadt.maki.simonstrator.api.Event;
import de.tudarmstadt.maki.simonstrator.api.EventHandler;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;
import de.tudarmstadt.maki.simonstrator.api.Randoms;
import de.tudarmstadt.maki.simonstrator.api.Time;

/**
 * The AbstractWaypointMovementModel can be used to implement movement models
 * based on way point data. It uses an implementation of LocalMovementStrategy
 * for the movement between selected way points.
 * 
 * @author Fabio Zöllner
 * @version 1.0, 27.03.2012
 */
public abstract class AbstractWaypointMovementModel implements MovementModel {

	private Set<SimLocationActuator> components = new LinkedHashSet<SimLocationActuator>();

	protected PositionVector worldDimensions;

	protected WeakHashMap<SimLocationActuator, PositionVector> destinations;

	protected WeakHashMap<SimLocationActuator, Long> pauseTimes;

	protected WeakHashMap<SimLocationActuator, Long> pauseInProgressTimes;

	protected WaypointModel waypointModel;

	protected LocalMovementStrategy localMovementStrategy;

	private int configurationCounter = 100;

	private long timeBetweenMovement = 1 * Time.SECOND;

	private double speedLimit = 1;

	private double unscaledSpeedLimit = speedLimit;

	private Random rnd = Randoms.getRandom(AbstractWaypointMovementModel.class);
	
	public AbstractWaypointMovementModel(double worldX, double worldY) {
		worldDimensions = new PositionVector(worldX, worldY);
		destinations = new WeakHashMap<SimLocationActuator, PositionVector>();
		pauseTimes = new WeakHashMap<SimLocationActuator, Long>();
		pauseInProgressTimes = new WeakHashMap<SimLocationActuator, Long>();

		// Simulator.registerAtEventBus(this);
	}

	@Configure()
	@After(required = { WaypointModel.class, MapLoader.class })
	public boolean configure() {
		if (this.waypointModel == null && configurationCounter > 0) {
			configurationCounter--;
			return false;
		}

		if (this.waypointModel == null) {
			Monitor.log(
					AbstractWaypointMovementModel.class,
					Level.INFO,
					"No waypoint model has been configured. Thus the movement speed won't be adjusted for the scale of the waypoint model.");
		}
		//
		// if (waypointModel.getScaleFactor() != 1.0) {
		// Simulator.postEvent(new ScaleWorldEvent(waypointModel
		// .getScaleFactor()));
		// }

		return true;
	}
	
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
	public void move() {
		long nrOfSteps = timeBetweenMovement / Time.SECOND;

		for (int i = 0; i < nrOfSteps; i++) {
			step();
		}
	}

	private void step() {
		Set<SimLocationActuator> comps = getComponents();
		for (SimLocationActuator mcomp : comps) {

			Long currentPause = pauseInProgressTimes.get(mcomp);
			if (currentPause != null) {
				if (Time.getCurrentTime() >= currentPause) {
					Monitor.log(AbstractWaypointMovementModel.class,
							Level.DEBUG, "Pause time ended...");
					pauseInProgressTimes.remove(mcomp);
				} else
					continue;
			}

			PositionVector dst = getDestination(mcomp);

			// If the movement model gave null as a destination no move is
			// executed
			if (dst == null) {
				Monitor.log(AbstractWaypointMovementModel.class, Level.DEBUG,
						"No destination before reachedPosition check... continuing");
				continue;
			}

			// If the position has been reached last round
			// set pause active and get a new destination
			if (reachedPosition(mcomp, dst)) {
				pauseAndGetNextPosition(mcomp);
				continue;
			}

			// Ask the local movement strategy for the next position.
			// It may return the next position or a boolean with true to notify
			// the
			// movement model that it can't get any closer to the current way
			// point.
			Either<PositionVector, Boolean> either = localMovementStrategy
					.nextPosition(mcomp, dst);

			if (either.hasLeft()) {
				updatePosition(mcomp, either.getLeft());
			} else {
				if (either.getRight().booleanValue()) {
					pauseAndGetNextPosition(mcomp);
					continue;
				} else {
					// Pause this round
					continue;
				}
			}
		}
	}
	
	/**
	 * Call this method to finally update the location of the given component.
	 * 
	 * @param actuator
	 * @param newPosition
	 */
	protected void updatePosition(SimLocationActuator actuator,
			PositionVector newPosition) {
		this.updatePosition(actuator, newPosition.getLongitude(), newPosition.getLatitude());
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
	 * Sets the pause time for the given component and calls nextPosition on it.
	 * 
	 * @param comp
	 */
	private void pauseAndGetNextPosition(SimLocationActuator comp) {
		Long pt = pauseTimes.get(comp) * Time.SECOND;
		Monitor.log(AbstractWaypointMovementModel.class, Level.DEBUG,
				"Position reached... pause time is " + pt);
		if (pt != null) {
			Monitor.log(AbstractWaypointMovementModel.class, Level.DEBUG,
					"Simulator time: " + Time.getCurrentTime());
			Monitor.log(AbstractWaypointMovementModel.class, Level.DEBUG,
					"Pause time: " + pt);
			Monitor.log(AbstractWaypointMovementModel.class, Level.DEBUG,
					"Added up: " + (Time.getCurrentTime() + pt));
			pauseInProgressTimes.put(comp, Time.getCurrentTime() + pt);
		}

		nextPosition(comp);
	}

	/**
	 * Checks if the given component has reached its destination
	 * 
	 * @param comp
	 * @param dst
	 * @return Returns true if the destination was reached
	 */
	private boolean reachedPosition(SimLocationActuator comp, PositionVector dst) {
		PositionVector pos = comp.getRealPosition();

		double distance = pos.distanceTo(dst);

		// FIXME: Better detection?

		return (distance < getSpeedLimit() * 2);
	}

	/**
	 * Returns the current destination of the given component and calls
	 * nextPosition if it hasn't been set yet.
	 * 
	 * @param comp
	 * @return
	 */
	private PositionVector getDestination(SimLocationActuator comp) {
		PositionVector dst = destinations.get(comp);

		Monitor.log(AbstractWaypointMovementModel.class, Level.DEBUG, "Pos: "
				+ comp.getRealPosition());
		Monitor.log(AbstractWaypointMovementModel.class, Level.DEBUG, "Dst: "
				+ dst);

		if (dst == null) {
			Monitor.log(AbstractWaypointMovementModel.class, Level.DEBUG,
					"No destination, calling nextPosition()");
			nextPosition(comp);
			dst = destinations.get(comp);
			Monitor.log(AbstractWaypointMovementModel.class, Level.DEBUG,
					"New destination is: " + dst);
		}

		return dst;
	}

	/**
	 * This method can be called by the concrete implementation to let the
	 * AbstractWaypointMovementModel know the next destination and pause time.
	 * 
	 * @param comp
	 * @param destination
	 * @param pauseTime
	 */
	protected void nextDestination(SimLocationActuator comp,
			PositionVector destination, long pauseTime) {
		destinations.put(comp, destination);
		pauseTimes.put(comp, pauseTime);
	}

	/**
	 * Is to be implemented by the concrete movement model and will be called by
	 * the AbstractWaypointMovementModel to ask the concrete implementation for
	 * a new destination.
	 * 
	 * @param component
	 */
	public abstract void nextPosition(SimLocationActuator component);

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
		Monitor.log(AbstractWaypointMovementModel.class, Level.DEBUG,
				"AbstractMovementModel: Adding component to the movement model");
		components.add(component);
	}

	/**
	 * Get a valid delta-Vector (does not cross world-boundaries and does not
	 * exceed moveSpeedLimit)
	 * 
	 * @param oldPosition
	 * @return
	 */
	protected PositionVector getRandomDelta(PositionVector oldPosition) {
		return getRandomDelta(oldPosition, getSpeedLimit());
	}

	/**
	 * Get a valid delta-Vector, where each abs(entry) must not exceed maxLength
	 * 
	 * @param oldPosition
	 * @param maxLength
	 * @return
	 */
	protected PositionVector getRandomDelta(PositionVector oldPosition,
			double maxLength) {
		double[] delta = new double[oldPosition.getDimensions()];
		for (int i = 0; i < oldPosition.getDimensions(); i++) {
			int tries = 0;
			do {
				delta[i] = getRandomDouble(-maxLength, maxLength);
				if (++tries > 50) {
					delta[i] = 0;
					break;
				}
				// delta[i] = (r.nextInt(2 * getMoveSpeedLimit() + 1) -
				// getMoveSpeedLimit());
			} while (oldPosition.getEntry(i) + delta[i] > getWorldDimension(i)
					|| oldPosition.getEntry(i) + delta[i] < 0);
		}
		PositionVector deltaVector = new PositionVector(delta);
		return deltaVector;
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
	 * Returns a random int between from and to, including both interval ends!
	 * 
	 * @param from
	 * @param to
	 * @return
	 */
	protected int getRandomInt(int from, int to) {
		int intervalSize = Math.abs(to - from);
		return (rnd.nextInt(intervalSize + 1) + from);
	}

	protected double getRandomDouble(double from, double to) {
		double intervalSize = Math.abs(to - from);
		return (rnd.nextDouble() * intervalSize + from);
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

		this.timeBetweenMovement = timeBetweenMoveOperations;

		if (timeBetweenMoveOperations > 0) {
			reschedule();
		}
	}

	protected void reschedule() {
		Event.scheduleWithDelay(timeBetweenMovement, new PeriodicMove(), this,
				0);
	}

	@Deprecated
	public void setSpeedLimit(double speedLimit) {
		this.speedLimit = speedLimit;
		this.unscaledSpeedLimit = speedLimit;
	}

	@Deprecated
	public double getSpeedLimit() {
		return speedLimit;
	}

	public void setWorldX(double dimension) {
		this.worldDimensions.setEntry(0, dimension);
	}

	public void setWorldY(double dimension) {
		this.worldDimensions.setEntry(1, dimension);
	}

	public double getWorldDimension(int dim) {
		return worldDimensions.getEntry(dim);
	}

	public void setWorldZ(double dimension) {
		this.worldDimensions.setEntry(2, dimension);
	}

	public void setWaypointModel(WaypointModel model) {
		this.waypointModel = model;

		if (localMovementStrategy != null)
			localMovementStrategy.setWaypointModel(getWaypointModel());
	}

	public WaypointModel getWaypointModel() {
		return this.waypointModel;
	}

	public void setLocalMovementStrategy(LocalMovementStrategy strategy) {
		strategy.setWaypointModel(getWaypointModel());
		this.localMovementStrategy = strategy;
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
