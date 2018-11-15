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

import org.jgrapht.graph.DefaultWeightedEdge;


/**
 * Represents a simple weighted path between
 * two way points.
 * 
 * @author Fabio Zöllner
 * @version 1.0, 27.03.2012
 */
public class Path extends DefaultWeightedEdge {
	private Waypoint source;
	private Waypoint target;
	private double weight = 1;
	
	public Path(Waypoint source, Waypoint target) {
		this.source = source;
		this.target = target;
		
		double d = source.getPosition().distanceTo(target.getPosition());
		
		weight = Math.abs(1 - (1 / d));
	}
	
	@Override
    protected double getWeight() {
        return weight;
    }
	
	public Waypoint getSource() {
		return source;
	}
	
	public Waypoint getTarget() {
		return target;
	}
	
	public Waypoint getOtherEnd(Waypoint wp) {
		if (source.equals(wp))
			return target;
		
		return source;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		
		Path other = (Path) obj;
		if (getWeight() != other.getWeight() ||
				!source.equals(other.getSource()) ||
				!target.equals(other.getTarget()))
			return false;
		
		return true;
	}

	@Override
	public String toString() {
		return "Path[From: " + source + ", To: " + target + ", Weight: " + weight + "]";
	}
	
}
