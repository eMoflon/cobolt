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

package de.tud.kom.p2psim.api.topology.views;

import de.tud.kom.p2psim.api.linklayer.mac.Link;
import de.tud.kom.p2psim.api.linklayer.mac.MacAddress;
import de.tud.kom.p2psim.api.linklayer.mac.MacLayer;

/**
 * Determines the probability of a packet drop on a link.
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 24.07.2012
 */
public interface DropProbabilityDeterminator {

	/**
	 * Called for each host that is added to the TopologyView
	 * 
	 * @param mac
	 */
	public void onMacAdded(MacLayer mac, TopologyView viewParent);

	/**
	 * Return the drop-probability between 0.0 and 1.0
	 * 
	 * @param view
	 *            This object can be used to access advanced information such as
	 *            Positions. If you make extensive use of them, consider
	 *            implementing a cache to speed up the calculations.
	 * @param source
	 * @param destination
	 * @param link
	 *            in case a link object already exists (and should be updated),
	 *            it is passed. Otherwise: null.
	 * @return
	 */
	public double getDropProbability(TopologyView view, MacAddress source,
			MacAddress destination, Link link);

}
