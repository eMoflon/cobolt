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

package de.tud.kom.p2psim.api.topology.movement;

import de.tud.kom.p2psim.impl.topology.PositionVector;
import de.tudarmstadt.maki.simonstrator.api.component.sensor.location.LocationActuator;
import de.tudarmstadt.maki.simonstrator.api.component.sensor.location.LocationSensor;

/**
 * Identifier for Components or Applications that support movement. If for
 * instance a Device should be moved it has to extend this class and will be
 * notified upon each change of its position.
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 04/25/2011
 * @deprecated we will try to clean up this interface in favor of
 *             {@link LocationSensor} or {@link SimLocationSensor} and
 *             {@link LocationActuator}
 */
@Deprecated
public interface MovementSupported {

	/**
	 * Callback, if the position of this MovementSupported-Instance changed in
	 * this round (this is preferred over registering the MovementComponent as a
	 * {@link MovementListener})
	 */
	public void positionChanged();

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
	 * Has to return true, if movement is possible (for example Net Layer is
	 * online).
	 * 
	 * @return
	 */
	public boolean movementActive();

	/**
	 * Adds a listener that is called upon node movement
	 * 
	 * @param listener
	 */
	public void addMovementListener(MovementListener listener);

	/**
	 * Removes the movement listener
	 * 
	 * @param listener
	 */
	public void removeMovementListener(MovementListener listener);

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
