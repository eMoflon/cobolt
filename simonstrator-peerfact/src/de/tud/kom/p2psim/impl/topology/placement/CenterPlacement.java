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

package de.tud.kom.p2psim.impl.topology.placement;

import de.tud.kom.p2psim.api.topology.TopologyComponent;
import de.tud.kom.p2psim.api.topology.placement.PlacementModel;
import de.tud.kom.p2psim.impl.topology.PositionVector;

/**
 * Places components in the middle of the map
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, Oct 21, 2013
 */
public class CenterPlacement implements PlacementModel {

	@Override
	public void addComponent(TopologyComponent comp) {
		//
	}

	@Override
	public PositionVector place(TopologyComponent comp) {
		PositionVector world = comp.getTopology().getWorldDimensions().clone();
		world.multiplyScalar(0.5);
		return world;
	}

}
