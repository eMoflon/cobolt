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

import de.tud.kom.p2psim.api.topology.movement.SimLocationActuator;
import de.tud.kom.p2psim.api.topology.movement.local.LocalMovementStrategy;
import de.tud.kom.p2psim.api.topology.obstacles.ObstacleModel;
import de.tud.kom.p2psim.api.topology.waypoints.WaypointModel;
import de.tud.kom.p2psim.impl.topology.PositionVector;
import de.tud.kom.p2psim.impl.util.Either;

public abstract class AbstractLocalMovementStrategy implements
		LocalMovementStrategy {

	protected WaypointModel waypointModel;

	protected ObstacleModel obstacleModel;

	private double scaleFactor = 1;

	public double getMovementSpeed(SimLocationActuator ms) {
		/*
		 * FIXME BR: why exactly is only the maxSpeed considered here?
		 */
		return ms.getMovementSpeed() * scaleFactor;
	}

	public void setScaleFactor(double scaleFactor) {
		this.scaleFactor = scaleFactor;
	}

	@Override
	public void setWaypointModel(WaypointModel model) {
		this.waypointModel = model;
	}

	@Override
	public void setObstacleModel(ObstacleModel model) {
		this.obstacleModel = model;
	}

	@Override
	public abstract Either<PositionVector, Boolean> nextPosition(
			SimLocationActuator comp, PositionVector destination);

}
