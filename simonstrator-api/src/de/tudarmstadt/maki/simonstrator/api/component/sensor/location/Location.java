/*
 * Copyright (c) 2005-2010 KOM – Multimedia Communications Lab
 *
 * This file is part of Simonstrator.KOM.
 * 
 * Simonstrator.KOM is free software: you can redistribute it and/or modify
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

package de.tudarmstadt.maki.simonstrator.api.component.sensor.location;

import de.tudarmstadt.maki.simonstrator.api.common.Transmitable;

/**
 * Again, based on Android.Location. Does not support direction and speed at the
 * moment.
 * 
 * @see http://developer.android.com/reference/android/location/Location.html
 * 
 * @author Bjoern Richerzhagen
 * 
 */
public interface Location extends Transmitable, Cloneable {

	/**
	 * Sets the contents of this location object to the specified location
	 * 
	 * @param l
	 */
	public void set(Location l);

	/**
	 * Latitude in degrees
	 * 
	 * Consider using only the "relative" methods distanceTo and bearingTo
	 * instead of absolute coordinates, whenever it is possible. This avoids
	 * conflicts in simulations on planar scenarios.
	 * 
	 * @return
	 */
	public double getLatitude();

	/**
	 * Longitude in degrees
	 * 
	 * Consider using only the "relative" methods distanceTo and bearingTo
	 * instead of absolute coordinates, whenever it is possible. This avoids
	 * conflicts in simulations on planar scenarios.
	 * 
	 * @return
	 */
	public double getLongitude();

	/**
	 * Returns the elapsed time since this location was retrieved (measured)
	 * 
	 * @return
	 */
	public long getAgeOfLocation();

	/**
	 * 
	 * Distance to the destination in meters
	 * 
	 * @param dest
	 * @return distance to the destination in meters
	 */
	public double distanceTo(Location dest);

	/**
	 * 
	 * Angle to the destination in degrees
	 * 
	 * @param dest
	 * @return bearing angle between this and dest, counting clockwise from true
	 *         north.
	 */
	public float bearingTo(Location dest);

	/**
	 * Copy of the current location
	 * 
	 * @return
	 */
	public Location clone();

}
