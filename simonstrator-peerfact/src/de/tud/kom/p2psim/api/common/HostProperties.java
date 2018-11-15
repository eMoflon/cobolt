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

package de.tud.kom.p2psim.api.common;

import de.tudarmstadt.maki.simonstrator.api.component.transport.ConnectivityListener;

/**
 * Provides Methods to get some <i>cross-layer</i> information about a host.
 * There are many possible properties like the geographical position, maximum
 * and current upload rate, maximum and current download rate. Note that
 * depending on the actual components of a host some properties may be empty,
 * e.g. the euclidian point may be unknown/unset.
 * 
 * <p>
 * Despite the cross-layer information host properties is used to enable the
 * churn per host.
 * 
 * @author Sebastian Kaune
 * @author Konstantin Pussep
 * @version 3.0, 03.12.2007 TODO revise the description
 * @see ConnectivityListener
 */
public interface HostProperties {

	/**
	 * Whether this host should be affected by churn.
	 * 
	 * @param churn
	 *            - churn on/off flag
	 */
	public void setEnableChurn(boolean churn);

	/**
	 * Churn status of this host (churn means that a host goes on and offline
	 * during the simulation)
	 * 
	 * @return whether churn is enabled for this host
	 */
	public boolean isChurnAffected();

	/**
	 * The same IDs as used in the configuration files.
	 * 
	 * @return ID of the group this host belongs to
	 */
	public String getGroupID();

	/**
	 * Sets a custom property that can be accessed by other components.
	 * 
	 * @param key
	 * @param value
	 */
	public void setProperty(String key, String value);

	/**
	 * Returns a custom property.
	 * 
	 * @param key
	 * @return
	 */
	public String getProperty(String key);

	/**
	 * Sets the minimal movement speed
	 * 
	 * @param minSpeed
	 */
	public void setMinMovementSpeed(double minSpeed);

	/**
	 * Sets the maximal movement speed
	 * 
	 * @param maxSpeed
	 */
	public void setMaxMovementSpeed(double maxSpeed);

	/**
	 * Gets the minimal movement speed, if it is configured. Otherwise, it will
	 * be throws an exception, if the minimal movement speed is not configured.
	 * 
	 * @return the minimal movement speed
	 */
	public double getMinMovementSpeed();

	/**
	 * Gets the maximal movement speed, if it is configured. Otherwise, it will
	 * be throws an exception, if the maximal movement speed is not configured.
	 * 
	 * @return the maximal movement speed
	 */
	public double getMaxMovementSpeed();

	/**
	 * 
	 * Updates the minimal and maximal movement speeds from the action file.
	 * This will be linear updated how long the duration is. The number of steps
	 * gives the updates!<br>
	 * <p>
	 * Throws an exception, if the minimal or maximal movement speed is not
	 * configured.
	 * 
	 * @param min
	 *            The new minimal movement speed.
	 * @param max
	 *            The new maximal movement speed.
	 * @param duration
	 *            The duration to reach the new movement speeds.
	 * @param steps
	 *            The number of steps to reach the new min and max movement
	 *            speeds. The minimal value for this is 0. If it is 0, then it
	 *            will be updated at simTime + duration! If it is 1, then will
	 *            be updated the value at the half and at end of duration!
	 */
	public void updateMovementSpeed(double min, double max, long duration,
			int steps);
	
}