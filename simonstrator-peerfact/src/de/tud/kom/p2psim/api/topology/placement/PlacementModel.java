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

package de.tud.kom.p2psim.api.topology.placement;

import de.tud.kom.p2psim.api.topology.Topology;
import de.tud.kom.p2psim.api.topology.TopologyComponent;
import de.tud.kom.p2psim.api.topology.obstacles.Obstacle;
import de.tud.kom.p2psim.impl.topology.PositionVector;
import de.tud.kom.p2psim.impl.topology.TopologyFactory;

/**
 * Strategies and Distributions that place hosts in a {@link Topology}. These
 * may be either statistically or based on input such as a csv of coordinates.
 * Furthermore, they may support {@link Obstacle}s by placing hosts only on free
 * areas.
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 26.02.2012
 */
public interface PlacementModel {

	/**
	 * Register a component with the {@link PlacementModel}. This allows the
	 * model to gain a full view of the number of hosts before placing them
	 * (interesting for grid-like distributions). Placement itself is done as
	 * soon as the addedComponent-Callback in the TopologyListener for this
	 * Model fired.
	 * 
	 * This method is used in the {@link TopologyFactory}
	 * 
	 * @param component
	 * @return
	 */
	public void addComponent(TopologyComponent comp);

	/**
	 * Retrieve the Position-Vector for this component.
	 * 
	 * @param comp
	 */
	public PositionVector place(TopologyComponent comp);

}
