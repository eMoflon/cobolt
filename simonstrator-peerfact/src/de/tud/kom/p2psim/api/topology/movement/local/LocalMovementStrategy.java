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

package de.tud.kom.p2psim.api.topology.movement.local;

import de.tud.kom.p2psim.api.topology.movement.SimLocationActuator;
import de.tud.kom.p2psim.api.topology.obstacles.ObstacleModel;
import de.tud.kom.p2psim.api.topology.waypoints.WaypointModel;
import de.tud.kom.p2psim.impl.topology.PositionVector;
import de.tud.kom.p2psim.impl.util.Either;

/**
 * This interface provides method definitions for the implementation of a local
 * movement strategy used by the abstract waypoint movement model.
 * 
 * @author Fabio Zöllner
 * @version 1.0, 09.04.2012
 */
public interface LocalMovementStrategy {
	
	public double getMovementSpeed(SimLocationActuator ms);

	public void setWaypointModel(WaypointModel model);

	public void setObstacleModel(ObstacleModel model);

	public void setScaleFactor(double scaleFactor);

	/**
	 * This method is called by the abstract waypoint movement model to
	 * determine the next position on the way to the specified destination.
	 * 
	 * Return value: - Left new PositionVector with the next position - Right
	 * true: The destination has been reached false: Do nothing.
	 * 
	 * @param comp
	 * @param destination
	 * @return Either the new position or Boolean (true) if no further position
	 *         can be calculated
	 */
	public Either<PositionVector, Boolean> nextPosition(SimLocationActuator comp,
			PositionVector destination);
}
