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
 * Represents a node in the map graph.
 * 
 * @author Fabio Zöllner
 * @version 1.0, 09.04.2012
 * @param <T> Information object
 */
public abstract class Waypoint<T> {
	protected PositionVector position;
	protected T info = null; // Not included in equals

	public Waypoint(PositionVector position, T info) {
		this.position = position;
		this.info = info;
	}
	
	public Waypoint(PositionVector position) {
		this.position = position;
		this.info = null;
	}
	
	public PositionVector getPosition() {
		return position;
	}
	
	public T getInfo() {
		return this.info;
	}
	
	public String toString() {
		return "Waypoint" + (info == null ? "" : "<" + info.getClass().getSimpleName() + ">") + "[" + position.getX() + ", " + position.getY() + ", " + info + "]";
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		
		Waypoint other = (Waypoint) obj;
		if (!position.equals(other.getPosition()))
			return false;
		
		return true;
	}
	
	public abstract Waypoint<T> clone();

	public void setPosition(PositionVector position) {
		this.position = position;
	}
}
