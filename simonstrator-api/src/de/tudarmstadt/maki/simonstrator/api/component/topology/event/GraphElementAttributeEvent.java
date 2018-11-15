/*
 * Copyright (c) 2005-2010 KOM – Multimedia Communications Lab
 *
 * This file is part of Simonstrator.KOM.
 * 
 * Simonstrator.KOM is free software: you can redistribute it and/or modify
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

package de.tudarmstadt.maki.simonstrator.api.component.topology.event;

import de.tudarmstadt.maki.simonstrator.api.common.graph.GraphElementProperty;
import de.tudarmstadt.maki.simonstrator.api.common.graph.IElement;
import de.tudarmstadt.maki.simonstrator.api.component.topology.TopologyID;
import de.tudarmstadt.maki.simonstrator.api.component.topology.TopologyProvider;

public class GraphElementAttributeEvent<E extends IElement, T>
		extends TopologyElementChangeEvent<E> {

	private final GraphElementProperty<T> property;
	private T oldValue;

	public GraphElementAttributeEvent(final GraphElementProperty<T> property, final T oldValue, final E element,
			TopologyProvider topology, TopologyID topologyIdentifier) {
		super(topology, topologyIdentifier, element, TopologyEventTypes.SET);
		this.property = property;
		this.oldValue = oldValue;
	}

	public T getOldValue() {
		return oldValue;
	}

	public GraphElementProperty<T> getProperty() {
		return property;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("GraphElementAttributeEvent [\n  property=");
		builder.append(property);
		builder.append(",\n  oldValue=");
		builder.append(oldValue);
		builder.append("\n]");
		return builder.toString();
	}
}
