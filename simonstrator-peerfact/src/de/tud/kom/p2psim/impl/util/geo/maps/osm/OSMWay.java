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

import java.util.Map;
import java.util.Set;
import java.util.Vector;

import com.google.common.collect.Maps;

import de.tud.kom.p2psim.api.util.geo.maps.Node;
import de.tud.kom.p2psim.api.util.geo.maps.Way;

public class OSMWay implements Way {
	private String name;

	private Vector<Node> nodes = new Vector<Node>();

	private java.util.Map<String, String> attributes = Maps.newHashMap();

	/* (non-Javadoc)
	 * @see de.tud.kom.p2psim.impl.util.geo.maps.Way#setName(java.lang.String)
	 */
	@Override
	public void setName(String name) {
		this.name = name;
	}

	/* (non-Javadoc)
	 * @see de.tud.kom.p2psim.impl.util.geo.maps.Way#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see de.tud.kom.p2psim.impl.util.geo.maps.Way#addNode(de.tud.kom.p2psim.impl.util.geo.maps.OSMNode)
	 */
	@Override
	public void addNode(Node node) {
		this.nodes.add((OSMNode)node);
	}
	
	@Override
	public Vector<Node> getNodes() {
		return this.nodes;
	}

	/* (non-Javadoc)
	 * @see de.tud.kom.p2psim.impl.util.geo.maps.Way#setAttribute(java.lang.String, java.lang.String)
	 */
	@Override
	public void setAttribute(String name, String value) {
		attributes.put(name, value);
	}

	/* (non-Javadoc)
	 * @see de.tud.kom.p2psim.impl.util.geo.maps.Way#getAttribute(java.lang.String)
	 */
	@Override
	public String getAttribute(String name) {
		return attributes.get(name);
	}

	public void setAttributes(java.util.Map<String, String> attributes) {
		this.attributes = attributes;
	}
	
	public boolean containsAttribute(String name) {
		return attributes.containsKey(name);
	}
	
	public Set<String> getAttributeKeys() {
		return attributes.keySet();
	}

	public String toString() {
		return "Way[" + name + ", " + nodes.size() + ", " + attributes.size()
				+ "]";
	}

	@Override
	public Map<String, String> getAttributes() {
		return this.attributes;
	}
}