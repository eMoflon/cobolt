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
import de.tud.kom.p2psim.impl.network.modular.common.GeoToolkit;
import de.tud.kom.p2psim.impl.network.modular.db.NetMeasurementDB;
import de.tud.kom.p2psim.impl.network.modular.st.PositioningStrategy;
import de.tud.kom.p2psim.impl.topology.PositionVector;
import de.tudarmstadt.maki.simonstrator.api.component.sensor.location.Location;

/**
 * Applies a geographical position as defined by the GeoIP project.
 * 
 * Based on code from the GeoLocationOracle (unknown author, Gerald Klunker?) in the GNP net layer
 * @author Leo Nobach
 *
 */
public class GeographicalPositioning implements PositioningStrategy {

	@Override
	public GeographicalPosition getPosition(
			SimHost host,
			NetMeasurementDB db,
			de.tud.kom.p2psim.impl.network.modular.db.NetMeasurementDB.Host hostMeta) {
	
		if (hostMeta == null) throw new IllegalArgumentException("The geographical positioner can not access the required measurement database, which is not set.");
		
		return new GeographicalPosition(hostMeta);
		
	}
	
	public class GeographicalPosition extends PositionVector {

		private double latitude;
		private double longitude;

		public GeographicalPosition(NetMeasurementDB.Host hostMeta) {
			super(2);
			latitude = hostMeta.getLatitude();
			longitude = hostMeta.getLongitude();
			super.setEntry(0, hostMeta.getLongitude());
			super.setEntry(1, hostMeta.getLatitude());
		}
		
		/**
		 * Calculates the distance in meters (m) from one host to another,
		 * using the Haversine formula. The squashed shape of the earth into account
		 * (approximately)
		 * 
		 */
		@Override
		public double distanceTo(Location netPosition) {
			if (!(netPosition instanceof PositionVector))
				throw new AssertionError(
						"Can not calculate the distance between two different position classes: "
					+ this.getClass() + " and " + netPosition.getClass());
			PositionVector other = (PositionVector) netPosition;

			return GeoToolkit.getDistance(this.latitude, this.longitude,
					other.getEntry(1), other.getEntry(0));
		}

		@Override
		public int getTransmissionSize() {
			return 16; // 2 * double
		}

		public GeographicalPosition clone() {
			return this; // no clone needed
		}

	}

	@Override
	public void writeBackToXML(BackWriter bw) {
		//No simple/complex types to write back
	}

}