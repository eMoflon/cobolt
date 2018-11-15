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

package de.tud.kom.p2psim.api.topology.movement;

import de.tud.kom.p2psim.impl.topology.PositionVector;
import de.tudarmstadt.maki.simonstrator.api.component.sensor.location.LocationActuator;
import de.tudarmstadt.maki.simonstrator.api.component.sensor.location.LocationSensor;

/**
 * A version of {@link LocationSensor} that provides access to the underlying
 * {@link PositionVector} data.
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, Jun 14, 2016
 */
public interface SimLocationActuator extends LocationActuator {

	/**
	 * Generic position, this allows us to use the GNP or other models that
	 * support the Position-Interface. This call does not consume any energy,
	 * even if a Positioning-Device is specified in the EnergyModel. It should
	 * not be used inside an Application, as the more sophisticated way is to
	 * add a GPS-Component to the EnergyModel and call getEstimatedPosition()
	 * 
	 * <b>This is used in high-performance calculations inside the TopologyViews
	 * and should therefore NOT return copies but the object instance to prevent
	 * fetching the object after each movement!</b>
	 * 
	 * @return
	 */
	public PositionVector getRealPosition();
	
	/**
	 * Gets the minimum movement speed of this movable component.
	 * 
	 * @return The minimum movement speed
	 */
	public double getMinMovementSpeed();

	/**
	 * Gets the maximum movement speed of this movable component.
	 * 
	 * @return The maximum movement speed
	 */
	public double getMaxMovementSpeed();

	/**
	 * Gets the currently set movement speed for that node. Initialized with a
	 * random value between min and max. Use setMovementSpeed() to lateron
	 * change the value.
	 * 
	 * @return
	 */
	public double getMovementSpeed();

	/**
	 * Allows to set the movement speed in meter per second. Should be between
	 * min and max speed, but this is not enforced.
	 */
	public void setMovementSpeed(double speed);
	
}
