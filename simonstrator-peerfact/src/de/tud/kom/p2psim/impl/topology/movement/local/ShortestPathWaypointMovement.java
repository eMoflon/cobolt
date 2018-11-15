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

package de.tud.kom.p2psim.impl.topology.movement.local;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

import de.tud.kom.p2psim.api.topology.movement.SimLocationActuator;
import de.tud.kom.p2psim.impl.topology.PositionVector;
import de.tud.kom.p2psim.impl.topology.waypoints.graph.Path;
import de.tud.kom.p2psim.impl.topology.waypoints.graph.Waypoint;
import de.tud.kom.p2psim.impl.topology.waypoints.graph.WeakWaypoint;
import de.tud.kom.p2psim.impl.util.Either;
import de.tud.kom.p2psim.impl.util.Left;
import de.tud.kom.p2psim.impl.util.Right;

/**
 * This movement strategy uses the getShortestPath method implemented by the
 * waypoint model and moves along path given by this method.
 * 
 * @author Fabio Zöllner
 * @version 1.0, 10.04.2012
 */
public class ShortestPathWaypointMovement extends AbstractLocalMovementStrategy {

	// Contains the shortest path and the currently used path
	protected final WeakHashMap<SimLocationActuator, Integer> currentPath = new WeakHashMap<SimLocationActuator, Integer>();

	protected final WeakHashMap<SimLocationActuator, List<Waypoint>> dstPaths = new WeakHashMap<SimLocationActuator, List<Waypoint>>();

	// Used to check if the destination was altered by the waypoint movement
	// model
	protected WeakHashMap<SimLocationActuator, PositionVector> currentDestination = new WeakHashMap<SimLocationActuator, PositionVector>();

	/**
	 * Calculates the next position of the given movement supported component by
	 * using the shortest path to the waypoint closest to the destination.
	 */
	@Override
	public Either<PositionVector, Boolean> nextPosition(SimLocationActuator comp,
			PositionVector destination) {

		if (currentDestination.get(comp) == null
				|| !currentDestination.get(comp).equals(destination)) {
			currentDestination.put(comp, destination);

			calculateNextMovementPath(comp, destination);

			// If the list of the shortest path is empty the destination is the
			// same
			// as the current position. Thus tell the abstract waypoint model
			// that we
			// reached the destination.
			if (dstPaths.get(comp).size() == 0) {
				return new Right<PositionVector, Boolean>(true);
			}
		}

		int currentPathIdx = currentPath.get(comp);
		Waypoint currentWaypoint = dstPaths.get(comp).get(currentPathIdx);
		double speed = getMovementSpeed(comp);
		PositionVector newPosition = comp.getRealPosition().moveStep(
				currentWaypoint.getPosition(), speed);

		if (destinationWaypointReached(currentWaypoint, newPosition, speed)) {
			// We reached the next waypoint on the path to the destination
			// move to the next waypoint
			currentPath.put(comp, ++currentPathIdx);

			// The destination waypoint has been reached, tell the abstract
			// waypoint model
			if (dstPaths.get(comp).size() <= currentPathIdx) {
				currentPath.put(comp, --currentPathIdx);
				return new Right<PositionVector, Boolean>(true);
			}
		}

		return new Left<PositionVector, Boolean>(newPosition);
	}

	/**
	 * Finds the closest waypoints to the current position of the movement
	 * supported component as well as the final destination and searches for a
	 * path between the two waypoints using the underlying network of
	 * WeakWaypoints.
	 * 
	 * @param comp
	 * @param finalDestination
	 */
	protected void calculateNextMovementPath(SimLocationActuator comp,
			PositionVector finalDestination) {
		// Required for shortest path calculation
		Waypoint closestWaypointToCurrentPosition = waypointModel
				.getClosestWaypoint(comp.getRealPosition(), WeakWaypoint.class);
		Waypoint closestWaypointToDestination = waypointModel
				.getClosestWaypoint(finalDestination, WeakWaypoint.class);

		List<Path> shortestPath = waypointModel.getShortestPath(
				closestWaypointToCurrentPosition, closestWaypointToDestination);

		List<Waypoint> waypointList = buildWaypointList(
				closestWaypointToCurrentPosition, shortestPath);

		dstPaths.put(comp, waypointList);
		currentPath.put(comp, Integer.valueOf(0));
	}

	/**
	 * Build a list of waypoints that starts a the given starting waypoint based
	 * on the given list of paths.
	 * 
	 * @param start
	 * @param shortestPath
	 * @return
	 */
	protected List<Waypoint> buildWaypointList(Waypoint start,
			List<Path> shortestPath) {
		List<Waypoint> waypointList = new ArrayList<Waypoint>();

		Waypoint lastWaypoint = start;
		waypointList.add(start);

		for (Path p : shortestPath) {
			lastWaypoint = p.getOtherEnd(lastWaypoint);
			waypointList.add(lastWaypoint);
		}

		return waypointList;
	}

	/**
	 * Checks if the current destination waypoint has been reached by testing
	 * the distance between the current position an the current destination
	 * waypoint for distance < speedLimit * 2.
	 * 
	 * FIXME BR: why *2?
	 * 
	 * @param currentWaypoint
	 * @param newPosition
	 * @return
	 */
	protected boolean destinationWaypointReached(Waypoint currentWaypoint,
			PositionVector newPosition, double speed) {
		double distance = newPosition.distanceTo(currentWaypoint.getPosition());

		return (distance < speed * 2);
	}
}
