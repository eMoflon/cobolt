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

import de.tud.kom.p2psim.api.util.geo.maps.Node;
import de.tud.kom.p2psim.impl.topology.PositionVector;

public class OSMNode implements Node {
	private Long id;

	/**
	 * The real position of this node.
	 */
	private PositionVector position;

	/**
	 * The position of this node in relation to the map used by the simulator.
	 */
	private PositionVector world;

	/**
	 * Options and attributes of this node.
	 */
	private java.util.Map<String, String> options;

	public OSMNode(Long id) {
		this.id = id;
	}

	public PositionVector getPosition() {
		return position;
	}

	public void setPosition(PositionVector position) {
		this.position = position;
	}

	@Override
	public PositionVector getWorldPosition() {
		return world;
	}

	@Override
	public void setWorldPosition(PositionVector world) {
		this.world = world;
	}

	@Override
	public void setAttribute(String name, String value) {
		options.put(name, value);
	}
	
	@Override
	public String getAttribute(String name) {
		return options.get(name);
	}
	
	public void setAttributes(java.util.Map<String, String> attributeMap) {
		this.options = attributeMap;
	}
	
	public boolean containsAttribute(String name) {
		return options.containsKey(name);
	}
	
	public String toString() {
		return "Node[" + id + ", " + position + ", " + world + "]";
	}
}