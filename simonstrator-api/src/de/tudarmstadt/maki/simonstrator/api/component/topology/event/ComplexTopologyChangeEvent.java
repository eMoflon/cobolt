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

import de.tudarmstadt.maki.simonstrator.api.component.topology.TopologyID;
import de.tudarmstadt.maki.simonstrator.api.component.topology.TopologyProvider;

public class ComplexTopologyChangeEvent extends DeltaBasedTopologyEvent {

	private static final String INDENTATION_OF_CHILD_EVENTS = "  ";
	private Iterable<? extends DeltaBasedTopologyEvent> childEvents;

	public ComplexTopologyChangeEvent(TopologyProvider topologyProvider, TopologyID topologyIdentifier,
			final Iterable<? extends DeltaBasedTopologyEvent> childEvents) {
		super(topologyProvider, topologyIdentifier);
		this.childEvents = childEvents;
	}

	public Iterable<? extends DeltaBasedTopologyEvent> getChildEvents() {
		return childEvents;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ComplexTopologyChangeEvent [topology=");
		builder.append(topologyProvider);
		builder.append(", topoId=");
		builder.append(topologyIdentifier);
		builder.append(", childEvents=[\n");
		for (final DeltaBasedTopologyEvent childEvent : childEvents) {
			builder.append(INDENTATION_OF_CHILD_EVENTS);
			builder.append(childEvent.toString().replaceAll("\\n", "\n" + INDENTATION_OF_CHILD_EVENTS));
			builder.append("\n");
		}
		builder.append("]]");
		return builder.toString();
	}
}
