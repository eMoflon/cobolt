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

import java.util.LinkedHashMap;
import java.util.Map;

import de.tud.kom.p2psim.api.topology.TopologyComponent;
import de.tud.kom.p2psim.api.topology.placement.PlacementModel;
import de.tud.kom.p2psim.impl.network.modular.db.NetMeasurementDB;
import de.tud.kom.p2psim.impl.network.modular.st.positioning.GNPPositioning.GNPPosition;
import de.tud.kom.p2psim.impl.topology.PositionVector;
import de.tud.kom.p2psim.impl.topology.TopologyFactory;

/**
 * Adapter to use the GNP-Positioning in the Topology-Components.
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 24.07.2012
 */
public class GNPPlacement implements PlacementModel {
	
	private final Map<TopologyComponent, NetMeasurementDB.Host> hostMetas = new LinkedHashMap<TopologyComponent, NetMeasurementDB.Host>();

	@Override
	public void addComponent(TopologyComponent comp) {
		hostMetas.put(comp,
				TopologyFactory.getMeasurementDBHost(comp.getHost()));
	}
	
	@Override
	public PositionVector place(TopologyComponent comp) {
		return new GNPPosition(hostMetas.get(comp));
	}
	
}
