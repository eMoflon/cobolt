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

package de.tud.kom.p2psim.api.topology;

import de.tud.kom.p2psim.api.common.SimHostComponent;
import de.tud.kom.p2psim.api.topology.movement.SimLocationActuator;
import de.tud.kom.p2psim.api.topology.views.TopologyView;
import de.tudarmstadt.maki.simonstrator.api.component.topology.UnderlayTopologyProvider;

/**
 * One component for each host, holding the position. These are maintained
 * inside a {@link Topology} which exists globally. They also include a
 * reference to the topology.
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 21.02.2012
 */
public interface TopologyComponent extends SimHostComponent,
		UnderlayTopologyProvider, SimLocationActuator {

	/**
	 * Returns the Topology-Object that provides access to {@link TopologyView}s
	 * Note: the {@link Topology} is also available as a GlobalComponent via the
	 * Binder-class.
	 * 
	 * @return
	 * @deprecated use Binder.getComponent(Topology.class) instead!
	 */
	@Deprecated
	public Topology getTopology();

}
