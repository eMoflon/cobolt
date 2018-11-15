/*
 * Copyright (c) 2005-2010 KOM – Multimedia Communications Lab
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

package de.tud.kom.p2psim.impl.topology.movement.modular;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import de.tud.kom.p2psim.api.scenario.ConfigurationException;
import de.tud.kom.p2psim.api.topology.Topology;
import de.tud.kom.p2psim.api.topology.movement.MovementModel;
import de.tud.kom.p2psim.api.topology.movement.SimLocationActuator;
import de.tud.kom.p2psim.api.topology.movement.local.LocalMovementStrategy;
import de.tud.kom.p2psim.api.topology.placement.PlacementModel;
import de.tud.kom.p2psim.impl.simengine.Simulator;
import de.tud.kom.p2psim.impl.topology.PositionVector;
import de.tud.kom.p2psim.impl.topology.TopologyFactory;
import de.tud.kom.p2psim.impl.topology.movement.AbstractWaypointMovementModel;
import de.tud.kom.p2psim.impl.topology.movement.CsvMovement;
import de.tud.kom.p2psim.impl.topology.movement.NoMovement;
import de.tud.kom.p2psim.impl.topology.movement.RandomPathMovement;
import de.tud.kom.p2psim.impl.topology.movement.modular.attraction.AttractionGenerator;
import de.tud.kom.p2psim.impl.topology.movement.modular.attraction.AttractionPoint;
import de.tud.kom.p2psim.impl.topology.movement.modular.transition.FixedAssignmentStrategy;
import de.tud.kom.p2psim.impl.topology.movement.modular.transition.TransitionStrategy;
import de.tud.kom.p2psim.impl.topology.views.VisualizationTopologyView.VisualizationInjector;
import de.tud.kom.p2psim.impl.util.Either;
import de.tudarmstadt.maki.simonstrator.api.Binder;
import de.tudarmstadt.maki.simonstrator.api.Event;
import de.tudarmstadt.maki.simonstrator.api.EventHandler;
import de.tudarmstadt.maki.simonstrator.api.Randoms;

/**
 * Modular Movement Model uses different models/strategies to create a movement
 * model. In this implementation, it has 4 different models/strategies.
 * <p>
 * M0: AttractionGenerator -> Generates the {@link AttractionPoint}s and place
 * them on the map. The {@link AttractionPoint}s can be moved!
 * <p>
 * M1: A general {@link MovementModel}, like {@link RandomPathMovement} or
 * {@link NoMovement}. It takes the {@link AttractionPoint}s and move them
 * around the world.
 * <p>
 * M2: The {@link TransitionStrategy}! It takes the Hosts, which should be moved
 * around, but calculates only the assignment to the {@link AttractionPoint}s.
 * It doesn't move the Hosts! It will be only assignment a new AttractionPoint!
 * 
 * <p>
 * M3: The {@link LocalMovementStrategy} is responsible for the movement of the
 * Hosts. It moves the hosts to the assigned AttractionPoint, and if the
 * AttractionPoint has moved, then will be followed. The
 * {@link LocalMovementStrategy} will be called from the
 * {@link ModularMovementModel} to do a Movement!
 * <p>
 * This class contains all four components and manage the data exchange.
 * Additionally it contains an periodic operation, which handle the movement of
 * all hosts. This mean, that it will be call the {@link LocalMovementStrategy}
 * with the destination. Please take care, that the handling of the movement of
 * the AttractionPoints will be handled by the movement model in M1! <br>
 * Further it contains an offset for every Host, which will be added to the
 * destination point (AttractionPoint), so that not all hosts, which are
 * assigned to one {@link AttractionPoint}, lies on the same point.<br>
 * 
 * @author Christoph Muenker
 * @version 1.0, 02.07.2013
 */
public class ModularMovementModel implements MovementModel, EventHandler {

	private final int EVENT_MOVE = 1;

	private final int EVENT_INIT = 2;

	protected PositionVector worldDimensions;

	protected MovementModel movementModel = new NoMovement();

	protected TransitionStrategy transition;

	protected AttractionGenerator attractionGenerator;

	protected LocalMovementStrategy localMovementStrategy;

	private Set<SimLocationActuator> movementListeners = new LinkedHashSet<SimLocationActuator>();

	private Set<SimLocationActuator> moveableHosts = new LinkedHashSet<SimLocationActuator>();

	private Map<SimLocationActuator, PositionVector> offsetPosition = new LinkedHashMap<SimLocationActuator, PositionVector>();

	private boolean initialized = false;

	private long timeBetweenMoveOperation = Simulator.SECOND_UNIT;

	private Random rand;

	public ModularMovementModel() {
		this.worldDimensions = Binder.getComponentOrNull(Topology.class)
				.getWorldDimensions();
		this.rand = Randoms.getRandom(ModularMovementModel.class);

		// scheduling initalization!
		Event.scheduleImmediately(this, null, EVENT_INIT);
	}

	/**
	 * This Method will be not called from the Components. So we call this
	 * manually!
	 */
	public void initialize() {
		if (!initialized) {
			VisualizationInjector.injectComponent("AttractionPoints", -1,
					new ModularMovementModelViz(this), false);

			checkConfiguration();

			// FIXME NR: Special implementation for crater to give movement model
			// the mapping of APs to Hosts to enable offline/online going of the
			// respective nodes when necessary
			if (movementModel.getClass() == CsvMovement.class && transition.getClass() == FixedAssignmentStrategy.class) {
				CsvMovement csvMovement = (CsvMovement) movementModel;
				FixedAssignmentStrategy transitionStrategy = (FixedAssignmentStrategy) transition;
				csvMovement.addTransitionStrategy(transitionStrategy);				
			}

			// setWayPointModel
			localMovementStrategy.setObstacleModel(Binder
					.getComponentOrNull(Topology.class).getObstacleModel());
			localMovementStrategy.setWaypointModel(Binder
					.getComponentOrNull(Topology.class).getWaypointModel());

			if (movementModel instanceof AbstractWaypointMovementModel) {
				AbstractWaypointMovementModel awmm = (AbstractWaypointMovementModel) movementModel;
				awmm.setWaypointModel(Binder.getComponentOrNull(Topology.class)
						.getWaypointModel());
			}

			List<AttractionPoint> attractionPoints = attractionGenerator
					.getAttractionPoints();
			for (AttractionPoint att : attractionPoints) {
				movementModel.addComponent(att);
			}

			transition.setAttractionPoints(attractionPoints);
			for (SimLocationActuator ms : moveableHosts) {
				transition.addComponent(ms);
			}

			setTimeBetweenMoveOperations(timeBetweenMoveOperation);

			// initial move
			move();

			initialized = true;
		}
	}

	private void checkConfiguration() {
		if (localMovementStrategy == null) {
			throw new ConfigurationException(
					"LocalMovementStrategy is missing in ModularMovementModel!");
		}
		if (movementModel == null) {
			throw new ConfigurationException(
					"MovementModel is missing in ModularMovementModel!");
		}
		if (transition == null) {
			throw new ConfigurationException(
					"TransitionStrategy is missing in ModularMovementModel!");
		}
		if (attractionGenerator == null) {
			throw new ConfigurationException(
					"AttractionGenerator is missing in ModularMovementModel!");
		}
		if (movementModel instanceof AbstractWaypointMovementModel
				&& Binder.getComponentOrNull(Topology.class)
						.getWaypointModel() == null) {
			throw new ConfigurationException(
					"Missing WaypointModel for the ModuloarMovementModel.movementModel");
		}
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
		throw new UnsupportedOperationException();
	}

	@Override
	public void addComponent(SimLocationActuator comp) {
		moveableHosts.add(comp);
		offsetPosition.put(comp, randomOffsetVector());
	}

	@Override
	public void setTimeBetweenMoveOperations(long time) {
		if (time > 0) {
			this.timeBetweenMoveOperation = time;
		} else {
			throw new ConfigurationException(
					"time is negative for the Move Operations");
		}
	}

	private PositionVector randomOffsetVector() {
		double x = rand.nextGaussian() * 6;
		double y = rand.nextGaussian() * 6;

		return new PositionVector(x, y);
	}

	protected void move() {
		Map<SimLocationActuator, AttractionPoint> assigns = transition
				.getAssignments();
		for (Entry<SimLocationActuator, AttractionPoint> entry : assigns
				.entrySet()) {
			SimLocationActuator ms = entry.getKey();
			PositionVector attractionCenter = entry.getValue()
					.getRealPosition();
			PositionVector destination = new PositionVector(attractionCenter);
			destination.add(offsetPosition.get(ms));

			doLocalMovement(ms, destination);

		}

		Event.scheduleWithDelay(timeBetweenMoveOperation, this, null,
				EVENT_MOVE);
	}

	/**
	 * 
	 * Ask the local movement strategy for the next position. It may return the
	 * next position or a boolean with true to notify the movement model that it
	 * can't get any closer to the current way point.
	 * 
	 * @param ms
	 * @param destination
	 */
	private void doLocalMovement(SimLocationActuator ms,
			PositionVector destination) {

		Either<PositionVector, Boolean> either = localMovementStrategy
				.nextPosition(ms, destination);
		if (either.hasLeft()) {
			ms.updateCurrentLocation(either.getLeft().getLongitude(), either.getLeft().getLatitude());
		}
	}

	public void setMovementModel(MovementModel mm) {
		this.movementModel = mm;
	}

	public void setAttractionGenerator(AttractionGenerator attractionGenerator) {
		this.attractionGenerator = attractionGenerator;
	}

	public void setLocalMovementStrategy(
			LocalMovementStrategy localMovementStrategy) {
		this.localMovementStrategy = localMovementStrategy;
	}

	public void setTransitionStrategy(TransitionStrategy transition) {
		this.transition = transition;
	}

	@Override
	public void eventOccurred(Object content, int type) {
		if (type == EVENT_INIT) {
			initialize();
		} else if (type == EVENT_MOVE) {
			move();
		}
	}

	/**
	 * Only for visualization!
	 * 
	 * @return
	 */
	protected List<AttractionPoint> getAttractionPoints() {
		return new Vector<AttractionPoint>(transition.getAssignments().values());
	}
}
