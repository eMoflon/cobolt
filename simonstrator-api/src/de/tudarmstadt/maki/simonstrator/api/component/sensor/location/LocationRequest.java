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

/**
 * Location Request, retrieve an object for manipulation through the
 * {@link LocationSensor}
 * 
 * @author Bjoern Richerzhagen
 * 
 */
public interface LocationRequest {

	public static final int PRIORITY_BALANCED_POWER_ACCURACY = 0x00000066;

	public static final int PRIORITY_HIGH_ACCURACY = 0x00000064;

	public static final int PRIORITY_NO_POWER = 0x00000069;

	/**
	 * Time between updates of the listener - not accurate (at least not on
	 * android), so do not rely on the listener for periodic operations.
	 * 
	 * @param interval
	 *            in Time.units
	 */
	public void setInterval(long interval);

	/**
	 * Priority of the request, regarding desired accuracy and energy
	 * consumption. Flags as defined by the android-SDK.
	 * 
	 * @param priority
	 *            one of the Flags provided in {@link LocationRequest}
	 */
	public void setPriority(int priority);

}
