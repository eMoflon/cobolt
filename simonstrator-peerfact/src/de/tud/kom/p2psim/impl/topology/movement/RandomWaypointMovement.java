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

package de.tud.kom.p2psim.impl.topology.movement;

import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;

import de.tud.kom.p2psim.api.scenario.ConfigurationException;
import de.tud.kom.p2psim.api.topology.movement.SimLocationActuator;
import de.tud.kom.p2psim.impl.topology.waypoints.graph.Waypoint;
import de.tud.kom.p2psim.impl.topology.waypoints.graph.WeakWaypoint;
import de.tudarmstadt.maki.simonstrator.api.Randoms;
import de.tudarmstadt.maki.simonstrator.api.util.XMLConfigurableConstructor;

public class RandomWaypointMovement extends AbstractWaypointMovementModel {
	private Random random = Randoms
			.getRandom(RandomWaypointMovement.class);
	
	@XMLConfigurableConstructor({"worldX", "worldY"})
	public RandomWaypointMovement(double worldX, double worldY) {
		super(worldX, worldY);
	}

	@Override
	public void nextPosition(SimLocationActuator component) {
		if (waypointModel == null) {
			throw new ConfigurationException("SLAWMovementModel requires a valid waypoint model which hasn't been provided, cannot execute");
		}

		List<Waypoint> waypoints = Lists.newArrayList(waypointModel.getWaypoints(WeakWaypoint.class));
		
		nextDestination(component, waypoints.get(random.nextInt(waypoints.size())).getPosition(), 0);
		
	}

}
