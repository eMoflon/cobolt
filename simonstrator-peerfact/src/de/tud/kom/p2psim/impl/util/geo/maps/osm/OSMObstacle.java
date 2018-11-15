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

package de.tud.kom.p2psim.impl.util.geo.maps.osm;

import java.util.List;

import com.google.common.collect.Maps;

import de.tud.kom.p2psim.impl.topology.PositionVector;
import de.tud.kom.p2psim.impl.topology.obstacles.PolygonObstacle;

public class OSMObstacle extends PolygonObstacle {
	private java.util.Map<String, String> attributes = Maps.newHashMap();

	public OSMObstacle(List<PositionVector> vertices) {
		super(vertices);
	}
	
	public OSMObstacle(List<PositionVector> vertices, double damping) {
		super(vertices, damping);
	}

	public void setAttribute(String name, String value) {
		attributes.put(name, value);
	}

	public String getAttribute(String name) {
		return attributes.get(name);
	}
	
	public void setAttributes(java.util.Map<String, String> attributeMap) {
		this.attributes = attributeMap;
	}
	
	public boolean containsAttribute(String name) {
		return attributes.containsKey(name);
	}
}
