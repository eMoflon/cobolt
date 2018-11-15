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
 * An implementation of this interface is passed to each topology view. It
 * determines how latencys are calculated (this could be distance based, based
 * on some kind of CDF or just static)
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 01.06.2012
 */
public interface LatencyDeterminator {

	/**
	 * Called for each host that is added to the TopologyView
	 * 
	 * @param mac
	 */
	public void onMacAdded(MacLayer mac, TopologyView viewParent);

	/**
	 * Return the latency in Simulator units between source and destination.
	 * This method is called once for every link, as soon as it is created and
	 * everytime it is updated (depends on the TopologyView)
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
	public long getLatency(TopologyView view, MacAddress source,
			MacAddress destination, Link link);

}
