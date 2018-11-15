/*
 * Copyright (c) 2005-2010 KOM - Multimedia Communications Lab
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

package de.tudarmstadt.maki.simonstrator.api.component.topology;

import de.tudarmstadt.maki.simonstrator.api.Graphs;
import de.tudarmstadt.maki.simonstrator.api.common.graph.DirectedEdge;
import de.tudarmstadt.maki.simonstrator.api.common.graph.GenericGraphElementProperties;
import de.tudarmstadt.maki.simonstrator.api.common.graph.IEdge;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INodeID;

/**
 * Represents an edge operations, which can be either an addition of the given
 * edge or a removal.
 * 
 * @author Julian M. Klomp
 *
 */
public class OperationalEdge extends DirectedEdge {

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		OperationalEdge other = (OperationalEdge) obj;
		if (type != other.type)
			return false;
		return true;
	}

	public enum EdgeOperationType {
		Add, Remove
	};

	private EdgeOperationType type;
	
	public OperationalEdge(IEdge edge, EdgeOperationType type) {
		super(edge.fromId(), edge.toId());
		this.setProperty(GenericGraphElementProperties.WEIGHT,
				edge.getProperty(GenericGraphElementProperties.WEIGHT));
		this.type = type;
	}

	public OperationalEdge(INodeID startNode, INodeID endNode,
			EdgeOperationType type) {
		super(startNode, endNode);
		this.type = type;
	}

	public EdgeOperationType getType() {
		return this.type;
	}

	public void setType(EdgeOperationType type) {
		this.type = type;
	}

	public IEdge getEdge() {
		return Graphs.createDirectedWeightedEdge(this.fromId(), this.toId(),
				this.getProperty(GenericGraphElementProperties.WEIGHT));
	}

	@Override
	public String toString() {
		return this.getType() + ": " + this.fromId() + " -> " + this.toId();
	}
}
