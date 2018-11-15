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

package de.tud.kom.p2psim.impl.topology.waypoints.graph;

import de.tud.kom.p2psim.impl.topology.PositionVector;

/**
 * The weak waypoint is intended to provide additional movement
 * information that can be utilized by localized movement models.
 * 
 * @author Fabio Zöllner
 * @version 1.0, 27.03.2012
 */
public class WeakWaypoint<T> extends Waypoint<T> {

	public WeakWaypoint(PositionVector position) {
		super(position);
	}
	
	public WeakWaypoint(PositionVector position, T info) {
		super(position, info);
	}

	public WeakWaypoint(double x, double y) {
		this(new PositionVector(x, y));
	}

	public WeakWaypoint<T> clone() {
		return new WeakWaypoint<T>(position.clone(), info);
	}
}
