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

package de.tud.kom.p2psim.impl.topology.waypoints;

import de.tud.kom.p2psim.api.topology.obstacles.ObstacleModel;
import de.tud.kom.p2psim.impl.topology.waypoints.strong.StrongWaypointStrategy;

public class StrongWaypointModel extends AbstractWaypointModel implements
		StrongWaypointSupport {

	// The number of strong waypoints that shall be added to the map
	protected int noOfWaypoints = 1000;

	protected StrongWaypointStrategy strongWaypointStrategy = null;

	/**
	 * Returns the number of strong waypoints that shall be generated for this
	 * model.
	 * 
	 * @return
	 */
	public int getNoOfWaypoints() {
		return this.noOfWaypoints;
	}

	/**
	 * Setter to allow changing the number of strong way points through the
	 * config.
	 * 
	 * @param noOfWaypoints
	 */
	public void setNoOfWaypoints(int noOfWaypoints) {
		this.noOfWaypoints = noOfWaypoints;
	}

	@Override
	public void init() {
		if (strongWaypointStrategy != null) {
			strongWaypointStrategy.generateStrongWaypoints(this);
		}
	}

	@Override
	public void generateWaypoints() {
		//
	}

	/**
	 * Sets the strong waypoint strategy that is used by the abstract waypoint
	 * model to generate strong waypoints in addition the the weak waypoints.
	 */
	public void setStrongWaypointStrategy(
			StrongWaypointStrategy strongWaypointStrategy) {
		this.strongWaypointStrategy = strongWaypointStrategy;
	}

	@Override
	public void setObstacleModel(ObstacleModel model) {
		super.setObstacleModel(model);

		if (this.strongWaypointStrategy != null) {
			this.strongWaypointStrategy.setObstacleModel(model);
		}
	}
}
