/*
 * Copyright (c) 2005-2011 KOM - Multimedia Communications Lab
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

package de.tud.kom.p2psim.impl.topology.placement;

import java.util.HashMap;
import java.util.Map;

import de.tud.kom.p2psim.api.topology.TopologyComponent;
import de.tud.kom.p2psim.api.topology.placement.PlacementModel;
import de.tud.kom.p2psim.impl.topology.PositionVector;

/**
 * Unified interface for Position-Distributions based on PositionVectors. When
 * used, it has to be initialized with the correct number of dimensions used by
 * the vectors. This is a subclass of {@link PlacementModel}s that are not
 * interested in the given host.
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 04/26/2011
 */
public abstract class PositionDistribution implements PlacementModel {

	private PositionVector worldDimensions = null;

	private Map<TopologyComponent, PositionVector> positions = new HashMap<TopologyComponent, PositionVector>();

	public PositionDistribution() {
		//
	}

	/**
	 * The PositionVector contains the upper bounds for all dimensions
	 * 
	 * @param dimensions
	 */
	public void setDimensions(PositionVector dimensions) {
		this.worldDimensions = dimensions;
	}

	protected int getDimensions() {
		return worldDimensions.getDimensions();
	}

	@Override
	public void addComponent(TopologyComponent comp) {
		if (worldDimensions == null) {
			worldDimensions = comp.getTopology().getWorldDimensions();
		}
		positions.put(comp, getNextPosition());
	}

	protected int getNumberOfComponents() {
		return positions.size();
	}

	protected PositionVector getWorldDimensions() {
		return worldDimensions;
	}

	@Override
	public PositionVector place(TopologyComponent comp) {
		return positions.get(comp);
	}

	/**
	 * Returns a n-Dimensional PositionVector, where n is the number of
	 * dimensions set with setDimensions()
	 * 
	 * @return
	 */
	public abstract PositionVector getNextPosition();

}
