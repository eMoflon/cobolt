/*
 * Copyright (c) 2005-2010 KOM - Multimedia Communications Lab
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

package de.tud.kom.p2psim.impl.topology.monitoring;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import de.tudarmstadt.maki.simonstrator.api.Message;
import de.tudarmstadt.maki.simonstrator.api.common.graph.Graph;
import de.tudarmstadt.maki.simonstrator.api.common.graph.IEdge;
import de.tudarmstadt.maki.simonstrator.api.common.graph.IElement;

public class ViewMessage implements Message {
	private Graph graph;

	private Map<IElement, Long> udgTimestamps;

	private Map<IEdge, Long> filterTimestamps;

	private LinkedList<IEdge> filteredEdges;

	public ViewMessage(Graph graph, Map<IElement, Long> udgStamps,
			LinkedList<IEdge> filteredEdges,
			Map<IEdge, Long> filterStamps) {
		this.graph = graph;
		this.udgTimestamps = udgStamps;
		this.filterTimestamps = filterStamps;
		this.filteredEdges = filteredEdges;
	}

	@Override
	public long getSize() {
		final long macTransmissionSize = 6;
		return macTransmissionSize
				* (graph.getNodes().size() + graph.getEdges().size() * 2 + filteredEdges
						.size() * 2) + udgTimestamps.size() * 4
				+ filterTimestamps.size() * 4;
	}

	@Override
	public Message getPayload() {
		return null;
	}

	public Graph getGraph() {
		return this.graph;
	}

	public Map<IElement, Long> getUdgTimestamps() {
		return udgTimestamps;
	}
	
	public Map<IEdge, Long> getFilterTimestamps() {
		return filterTimestamps;
	}
	
	public Collection<IEdge> getFilteredEdges() {
		return filteredEdges;
	}
}