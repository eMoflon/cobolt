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

package de.tud.kom.p2psim.impl.network.modular.st.positioning;

import de.tud.kom.p2psim.api.common.SimHost;
import de.tud.kom.p2psim.impl.network.modular.db.NetMeasurementDB;
import de.tud.kom.p2psim.impl.network.modular.st.PositioningStrategy;
import de.tud.kom.p2psim.impl.topology.placement.PositionDistribution;
import de.tudarmstadt.maki.simonstrator.api.component.sensor.location.Location;

/**
 * Positioning on a 2D-Plane, used for all Movement-Enabled Routing-Scenarios
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 04/25/2011
 */
public class PlanePositioning implements PositioningStrategy {

	private PositionDistribution distribution;

	@Override
	public void writeBackToXML(BackWriter bw) {
		// nothing to configure here
	}

	@Override
	public Location getPosition(
			SimHost host,
			NetMeasurementDB db,
			de.tud.kom.p2psim.impl.network.modular.db.NetMeasurementDB.Host hostMeta) {
		if (distribution == null) {
			throw new AssertionError(
					"You have to specify an IPositionDistribution when using DeviceDependentPositioning!");
		}
		return distribution.getNextPosition();
	}

	/**
	 * Specify a Distribution for Positions of Hosts
	 * 
	 * @param distribution
	 */
	public void setPositionDistribution(PositionDistribution distribution) {
		this.distribution = distribution;
	}

}
