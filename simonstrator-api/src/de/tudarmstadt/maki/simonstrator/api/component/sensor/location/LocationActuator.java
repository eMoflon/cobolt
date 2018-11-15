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
 * Actuator for location information, allowing "someone" to change a host's
 * current location. While this might not be relevant on prototypical
 * deployments, it allows us to integrate movement into applications and
 * demonstrations without interfacing directly with the platform itself.
 * 
 * @author Bjoern Richerzhagen
 *
 */
public interface LocationActuator extends LocationSensor {

	/**
	 * Updates the node's current location with these coordinates. There is no
	 * guarantee that this operation will succeed. IF the current location
	 * changed, the {@link LocationSensor} will accurately reflect this.
	 * 
	 * @param longitude
	 *            (or x)
	 * @param latitude
	 *            (or y)
	 * 
	 */
	public void updateCurrentLocation(double longitude, double latitude);

	/**
	 * A way to interact with node movement from within applications and the
	 * like. This is an optional operation - it is expected to throw an
	 * {@link UnsupportedOperationException} if it is not supported.
	 * 
	 * @param longitude
	 * @param latitude
	 */
	public void setNewTargetLocation(double longitude, double latitude)
			throws UnsupportedOperationException;

}
