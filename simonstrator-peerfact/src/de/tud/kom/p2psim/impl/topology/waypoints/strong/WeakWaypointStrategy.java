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

package de.tud.kom.p2psim.impl.topology.waypoints.strong;

import java.util.List;

import com.google.common.collect.Lists;

import de.tud.kom.p2psim.api.topology.waypoints.WaypointModel;
import de.tud.kom.p2psim.impl.topology.waypoints.graph.StrongWaypoint;
import de.tud.kom.p2psim.impl.topology.waypoints.graph.Waypoint;
import de.tud.kom.p2psim.impl.topology.waypoints.graph.WeakWaypoint;

public class WeakWaypointStrategy extends StrongWaypointStrategy {
	// The number of strong waypoints that shall be added to the map
	protected int noOfWaypoints = 1000;

	@Override
	public void generateStrongWaypoints(WaypointModel wpModel) {
		List<Waypoint> waypoints = Lists.newArrayList(wpModel
				.getWaypoints(WeakWaypoint.class));

		int generatedWaypoints = 0;
		int index = 0;

		while (generatedWaypoints < noOfWaypoints) {
			wpModel.addWaypoint(new StrongWaypoint<Object>(waypoints.get(index)
					.getPosition()));
			generatedWaypoints++;
			index++;
			if (index >= waypoints.size())
				index = 0;
		}
	}

}
