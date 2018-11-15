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

import java.util.List;
import java.util.Vector;

import de.tud.kom.p2psim.api.common.SimHost;
import de.tud.kom.p2psim.impl.network.modular.common.GNPToolkit;
import de.tud.kom.p2psim.impl.network.modular.db.NetMeasurementDB;
import de.tud.kom.p2psim.impl.network.modular.st.PositioningStrategy;
import de.tud.kom.p2psim.impl.topology.PositionVector;
import de.tudarmstadt.maki.simonstrator.api.component.sensor.location.Location;

/**
 * Applies the (virtual) GNP position as the host's position
 * 
 * @author Leo Nobach
 * 
 */
public class GNPPositioning implements PositioningStrategy {

	@Override
	public Location getPosition(SimHost host, NetMeasurementDB db,
			NetMeasurementDB.Host hostMeta) {

		if (hostMeta == null)
			throw new IllegalStateException(
					"The GNP positioning strategy needs a measurement database to work properly.");

		return new GNPPosition(hostMeta);

	}

	public static class GNPPosition extends PositionVector {

		private List<Double> coords;

		public GNPPosition(NetMeasurementDB.Host hostMeta) {
			super(2);
			this.coords = hostMeta.getCoordinates();
			super.setEntry(0, coords.get(0));
			super.setEntry(1, coords.get(1));
		}

		@Override
		public double distanceTo(Location netPosition) {
			if (!(netPosition instanceof PositionVector))
				throw new AssertionError(
						"Can not calculate distances between different position classes: "
								+ this.getClass() + " and "
								+ netPosition.getClass());
			PositionVector other = (PositionVector) netPosition;
			List<Double> otherCoords = new Vector<Double>();
			otherCoords.add(other.getEntry(0));
			otherCoords.add(other.getEntry(1));

			return GNPToolkit.getDistance(this.coords, otherCoords);
		}

		@Override
		public int getTransmissionSize() {
			return coords.size() * 8; // * double
		}

		public List<Double> getCoords() {
			return coords;
		}

		@Override
		public GNPPosition clone() {
			return this; // no need to clone
		}

	}

	@Override
	public void writeBackToXML(BackWriter bw) {
		// No simple/complex types to write back
	}

}
