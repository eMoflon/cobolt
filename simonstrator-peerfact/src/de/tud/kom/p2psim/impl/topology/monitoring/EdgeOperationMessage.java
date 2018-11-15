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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import de.tudarmstadt.maki.simonstrator.api.Message;
import de.tudarmstadt.maki.simonstrator.api.component.topology.OperationalEdge;

public class EdgeOperationMessage implements Message {

	private Collection<OperationalEdge> edgeOperations;
	
	private int hopsToLive;
	
	public EdgeOperationMessage(Collection<OperationalEdge> edgeOperations) {
		this(edgeOperations,1);
	}
	
	public EdgeOperationMessage(Collection<OperationalEdge> edgeOperations, int hopsToLive) {
		this.edgeOperations = edgeOperations;
		this.setHopsToLive(hopsToLive);
	}
	
	public EdgeOperationMessage(OperationalEdge edgeOperation) {
		this(edgeOperation,1);
	}
	
	public EdgeOperationMessage(OperationalEdge edgeOperation, int hopsToLive) {
		this.edgeOperations = new LinkedList<OperationalEdge>();
		edgeOperations.add(edgeOperation);
		this.setHopsToLive(hopsToLive);
	}
	
	@Override
	public long getSize() {
		final long macTransmissionSize = 6;
		return macTransmissionSize * (edgeOperations.size()) + macTransmissionSize;
	}

	@Override
	public Message getPayload() {
		return null;
	}
	

	public Collection<OperationalEdge> getOperationalEdges() {
		return edgeOperations;
	}
	
	
	public Collection<OperationalEdge> getEdgeOperations() {
		return getOperationalEdges();
	}

	private void setHopsToLive(int hopsToLive) {
		this.hopsToLive = hopsToLive;
	}	
	
	public void decreaseHopsToLive() {
		this.hopsToLive--;
	}
	
	public boolean isDead() {
		return this.hopsToLive < 1;
	}
	
	@Override
	public String toString() {
		return "EoM ("+this.hopsToLive+"): " + this.getOperationalEdges();
	}
	
	public EdgeOperationMessage copy() {
		Set<OperationalEdge> operations = new HashSet<OperationalEdge>(this.edgeOperations);
		EdgeOperationMessage cpy = new EdgeOperationMessage(operations,this.hopsToLive);
		return cpy;
	}
}
