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
import java.util.Map.Entry;

import de.tudarmstadt.maki.simonstrator.api.component.sis.type.SiSType;

/**
 * Represents a directed edge in a topology
 *
 * @author Michael Stein
 */
public class DirectedEdge implements IEdge {

	/**
	 * @deprecated Use {@link GenericGraphElementProperties#WEIGHT}
	 */
	@Deprecated
	public static final GraphElementProperty<Double> WEIGHT = GenericGraphElementProperties.WEIGHT;

	private final INodeID startNode;
	private final INodeID endNode;
	private final EdgeID edgeId;

	/**
	 * Node properties (attached objects)
	 */
	private final Map<SiSType<?>, Object> properties = new LinkedHashMap<>();

	/**
	 * Creates an unweighted, directed edge
	 */
	public DirectedEdge(INodeID startNode, INodeID endNode) {
		this(startNode, endNode, EdgeID.get(startNode, endNode));
	}

	public DirectedEdge(INodeID startNode, INodeID endNode, EdgeID edgeId) {
		this.startNode = startNode;
		this.endNode = endNode;
		this.edgeId = edgeId;
	}

	/**
	 * Creates an unweighted, directed edge
	 */
	public DirectedEdge(Node startNode, Node endNode, String id) {
		this(startNode.getId(), endNode.getId(), EdgeID.get(id));
	}

	public DirectedEdge(INodeID startNode, INodeID endNode, String id) {
		this(startNode, endNode, EdgeID.get(id));
	}

	@Override
	public INodeID fromId() {
		return startNode;
	}

	@Override
	public INodeID toId() {
		return endNode;
	}

	@Override
	public EdgeID getId() {
		return this.edgeId;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getProperty(SiSType<T> property) {
		return (T) properties.get(property);
	}

	@Override
	public <T> IEdge setProperty(SiSType<T> property, T value) {
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
		return "DirectedEdge [" + this.getId() + (startNode != null ? ", src: " + startNode : "")
				+ (endNode != null ? ", trg:" + endNode : "")
				+ (properties != null && !properties.isEmpty() ? ", properties=" + formatProperties() : "")
				+ "]";
	}

	public String formatProperties() {
		final StringBuilder builder = new StringBuilder();
		builder.append("[");
		for (final Entry<SiSType<?>, Object> entry : this.properties.entrySet()) {
			/*
			 * Special treatment to avoid infinite loops if the property is of
			 * type IEdge (e.g. if it represents the reverse edge of this edge).
			 */
			final Object value = entry.getValue();
			final SiSType<?> key = entry.getKey();
			if (IEdge.class.isInstance(value))
			{
				builder.append(
						String.format("%s=%s, ", key.toString(), IEdge.class.cast(value).getId()));
			} else {
				builder.append(String.format("%s=%s, ", key.toString(), value.toString()));
			}
		}
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((endNode == null) ? 0 : endNode.hashCode());
		result = prime * result + ((startNode == null) ? 0 : startNode.hashCode());
		result = prime * result + ((this.getId() == null) ? 0 : this.getId().hashCode());
		return result;
	}

	/**
	 * Two {@link DirectedEdge}s are equal if their IDs are equal
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DirectedEdge other = (DirectedEdge) obj;
		if (endNode == null) {
			if (other.endNode != null)
				return false;
		} else if (!endNode.equals(other.endNode))
			return false;
		if (startNode == null) {
			if (other.startNode != null)
				return false;
		} else if (!startNode.equals(other.startNode))
			return false;
		if (getId() == null && other.getId() != null) {
				return false;
		} else if (!this.getId().equals(other.getId())) {
			return false;
		}
		return true;
	}

}
