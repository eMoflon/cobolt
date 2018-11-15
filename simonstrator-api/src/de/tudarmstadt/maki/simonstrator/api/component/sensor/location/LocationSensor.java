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

import de.tudarmstadt.maki.simonstrator.api.component.sensor.SensorComponent;

/**
 * Based on the Google Location Service (LocationClient) provided for the
 * Android Platform. Provides access to the last known location (energy
 * efficient) or QoS-Specifications for regular location updates.
 * 
 * @see http 
 *      ://developer.android.com/reference/com/google/android/gms/location/package
 *      -summary.html
 * @author Bjoern Richerzhagen
 * 
 */
public interface LocationSensor extends SensorComponent {

	/**
	 * Retrieve the last known location
	 * 
	 * @return
	 */
	public Location getLastLocation();

	/**
	 * Start receiving location updates with the specified accuracy or interval
	 * on the provided listener. The listener fires as soon as possible with a
	 * valid location (in simulations: immediately) and from then on with the
	 * desired interval lengths.
	 * 
	 * @param request
	 *            (can be null - in this case, the listener will only be
	 *            notified if some other location requests exists and if a new
	 *            location is determined without active search by this specific
	 *            request)
	 * @param listener
	 */
	public void requestLocationUpdates(LocationRequest request,
			LocationListener listener);

	/**
	 * Get rid of location updates for the corresponding listener (and save
	 * energy)
	 * 
	 * @param listener
	 */
	public void removeLocationUpdates(LocationListener listener);

	/**
	 * Retrieve a {@link LocationRequest} object for manipulation and usage with
	 * requestLocationUpdates
	 * 
	 * @return
	 */
	public LocationRequest getLocationRequest();

}
