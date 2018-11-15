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

package de.tudarmstadt.maki.simonstrator.api.common.graph;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import de.tudarmstadt.maki.simonstrator.api.component.sis.type.SiSType;


/**
 * Represents a node in a topology. Nodes are equal, if their ID is equal
 * (usually a plain net-ID). The attached properties are not considered for
 * equality and have to be merged or replaced based on the desired semantics.
 * 
 * @author michael.stein
 */
public class Node implements INode {

	/**
	 * The node identifier. Has to be unique.
	 */
	protected final INodeID id;

	/**
	 * Node properties (attached objects)
	 */
	private final Map<SiSType<?>, Object> properties = new LinkedHashMap<>();

	/**
	 * @param id
	 *            unique node identifier
	 */
	public Node(INodeID id) {
		this.id = id;
	}

	@Override
	public INodeID getId() {
		return id;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getProperty(SiSType<T> property) {
		return (T) properties.get(property);
	}

	@Override
	public <T> INode setProperty(SiSType<T> property, T value) {
		if (value == null) {
			properties.remove(property);
		} else {
			properties.put(property, value);
		}
		return this;
	}

	@Override
	public Map<SiSType<?>, Object> getProperties() {
		return Collections.unmodifiableMap(properties);
	}

	@Override
	public void addPropertiesFrom(IElement other) {
		properties.putAll(other.getProperties());
	}

	@Override
	public void clearProperties() {
		properties.clear();
	}

	@Override
	public String toString() {
		return "Node [" + (id != null ? "id=" + id : "")
				+ (properties != null && !properties.isEmpty() ?  ", properties=" + properties : "") + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	/**
	 * Two {@link Node}s are equal if their IDs are equal
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Node other = (Node) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
