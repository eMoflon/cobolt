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

package de.tud.kom.p2psim.impl.topology.views.fiveg;

import de.tud.kom.p2psim.impl.topology.views.FiveGTopologyView;
import de.tud.kom.p2psim.impl.topology.views.FiveGTopologyView.CellLink;

/**
 * Database for the {@link FiveGTopologyView} - containing a mapping of
 * position IDs to the respective link characteristics.
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, Nov 5, 2015
 */
public interface FiveGTopologyDatabase {

	/**
	 * Calculation of a segment ID based on a position (usually given in
	 * Cartesian coordinates between 0 and WORLD_SIZE)
	 * 
	 * @param x
	 *            coordinate
	 * @param y
	 *            coordinate
	 * @return
	 */
	public int getSegmentID(double x, double y);

	/**
	 * Access to the {@link Entry} for a given segmentID. To allow distinction
	 * between clouds (full latency) and cloudlets (less latency, as the backend
	 * is "shorter"), we added a boolean "isCloudlet", which may or may not be
	 * used by the database. It is expected that the link to a cloudlet should
	 * outperform the link to a cloud.
	 * 
	 * @param segmentID
	 * @return the entry. ONLY for access point databases, this might also
	 *         return null, if the given segment is not offering access point
	 *         connectivity.
	 */
	public FiveGTopologyDatabase.Entry getEntryFor(int segmentID,
			boolean isCloudlet);

	/**
	 * Data structure for the network parameters of a given segment ID - these
	 * are directly accessed by the {@link CellLink} object on each call (so
	 * they should not perform expensive calculations!). We allow a distinction
	 * between up and download (from the client's perspective) via the provided
	 * boolean - upload means: from client to station, download means: from
	 * station to client.
	 * 
	 * @author Bjoern Richerzhagen
	 * @version 1.0, Nov 5, 2015
	 */
	public interface Entry {

		/**
		 * Static segment ID
		 * 
		 * @return
		 */
		public int getSegmentID();

		/**
		 * Probability of a packet drop on the link - can even be 1, so that the
		 * link is virtually disconnected.
		 * 
		 * @return
		 */
		public double getDropProbability(boolean isUpload);

		/**
		 * Latency on the link (RTT/2)
		 * 
		 * @return
		 */
		public long getLatency(boolean isUpload);

		/**
		 * Bandwidth on the link
		 * 
		 * @return
		 */
		public long getBandwidth(boolean isUpload);

	}

}
