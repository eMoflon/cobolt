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

import de.tudarmstadt.maki.simonstrator.api.common.graph.IElement;
import de.tudarmstadt.maki.simonstrator.api.component.topology.TopologyID;
import de.tudarmstadt.maki.simonstrator.api.component.topology.TopologyProvider;

public abstract class TopologyElementChangeEvent<E extends IElement> extends DeltaBasedTopologyEvent {

	protected final E affectedElement;
	protected final TopologyEventType eventType;

	public TopologyElementChangeEvent(TopologyProvider topology, TopologyID topologyIdentifier, final E affectedElement,
			final TopologyEventType eventType) {
		super(topology, topologyIdentifier);
		this.affectedElement = affectedElement;
		this.eventType = eventType;
	}

	public E getAffectedElement() {
		return affectedElement;
	}

	public TopologyEventType getEventType() {
		return eventType;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TopologyElementChangeEvent [\n  affectedElement=");
		builder.append(affectedElement);
		builder.append(",\n  eventType=");
		builder.append(eventType);
		builder.append("\n]");
		return builder.toString();
	}

}
