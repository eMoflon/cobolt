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

import de.tud.kom.p2psim.api.topology.TopologyComponent;

/**
 * A movement model for a group of {@link TopologyComponent}s. The movement
 * model interacts with the topology to move hosts and to react to obstacles, if
 * needed.
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 21.02.2012
 */
public interface MovementModel {

	/**
	 * Register all {@link SimLocationActuator}s that are controlled by this
	 * movement model.
	 * 
	 * @param comp
	 */
	public void addComponent(SimLocationActuator actuator);

	/**
	 * Called for each component once during initialization, after all
	 * components have been added.
	 */
	public void placeComponent(SimLocationActuator actuator);

	/**
	 * OPTIONAL: tell the movement model to alter the current actuator's target
	 * location. If supported, the actuator will stop moving towards its old
	 * destination and start approaching the new destination instead.
	 * 
	 * OBVIOUSLY, this is not supported by all models (from a semantic point of
	 * view)
	 * 
	 * @param actuator
	 * @param longitude
	 * @param latitude
	 */
	public void changeTargetLocation(SimLocationActuator actuator,
			double longitude, double latitude);

	/**
	 * If you want to trigger the movement periodically, set this to a time
	 * 
	 * @param time
	 */
	public void setTimeBetweenMoveOperations(long time);

}
