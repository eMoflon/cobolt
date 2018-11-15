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


package de.tud.kom.p2psim.impl.network.gnp.topology;



import de.tud.kom.p2psim.impl.topology.PositionVector;
import de.tudarmstadt.maki.simonstrator.api.component.sensor.location.Location;

/**
 * Implementation of NetPosition for Position and distance measurnment on the
 * earth.
 * 
 * @author Gerald Klunker
 * @version 0.1, 05.02.2008
 * 
 */

public class GeographicPosition extends PositionVector {

	private double latitude;

	private double longitude;

	/**
	 * 
	 * @param longitude
	 * @param latitude
	 */
	public GeographicPosition(double longitude, double latitude) {
		super(new double[] { longitude, latitude });
		this.longitude = longitude;
		this.latitude = latitude;
	}

	/**
	 * @return geographical distance in km
	 */
	public double distanceTo(Location point) {
		double pi = 3.14159265;
		double radConverter = pi / 180;
		double lat1 = latitude * radConverter;
		double lat2 = ((GeographicPosition) point).getLatitude() * radConverter;
		double delta_lat = lat2 - lat1;
		double delta_lon = (((GeographicPosition) point).getLongitude() - longitude)
				* radConverter;
		double temp = Math.pow(Math.sin(delta_lat / 2), 2) + Math.cos(lat1)
				* Math.cos(lat2) * Math.pow(Math.sin(delta_lon / 2), 2);
		return 2 * 6378.2 * Math.atan2(Math.sqrt(temp), Math.sqrt(1 - temp));
	}

	@Override
	public int getTransmissionSize() {
		return 16; // 2 * double
	}

	public GeographicPosition clone() {
		return new GeographicPosition(longitude, latitude);
	}

	/**
	 * 
	 * @return latitude of position
	 */
	public double getLatitude() {
		return latitude;
	}

	/**
	 * 
	 * @return longitude of position
	 */
	public double getLongitude() {
		return longitude;
	}

}
