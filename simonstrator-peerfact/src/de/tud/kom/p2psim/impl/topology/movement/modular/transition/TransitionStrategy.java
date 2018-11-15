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

package de.tud.kom.p2psim.impl.topology.movement.modular.transition;

import java.util.List;
import java.util.Map;

import de.tud.kom.p2psim.api.topology.movement.SimLocationActuator;
import de.tud.kom.p2psim.impl.topology.movement.modular.ModularMovementModel;
import de.tud.kom.p2psim.impl.topology.movement.modular.attraction.AttractionPoint;

/**
 * This is the interface for the Transition Strategy.<br>
 * It derives automatically the assignments of the added
 * {@link SimLocationActuator} to the {@link AttractionPoint}s. This mean, that
 * the implementation must handle the new assignments after a certain time. The
 * {@link ModularMovementModel}, will be only call the {@link #getAssignments()}
 * in every MovementStep, to calculate the movement of the
 * {@link SimLocationActuator} Objects, in respect to the Assignment.
 * 
 * @author Christoph Muenker
 * @version 1.0, 25.06.2013
 */
public interface TransitionStrategy {
	/**
	 * Returns the assignments of the MovementSupported Objects to the
	 * AttractionPoints
	 * 
	 * @return
	 */
	public Map<SimLocationActuator, AttractionPoint> getAssignments();

	/**
	 * Should be called first, to add the Attraction Points for the assignment!
	 * 
	 * @param attractionPoints
	 */
	public void setAttractionPoints(List<AttractionPoint> attractionPoints);

	/**
	 * Add the {@link SimLocationActuator} object and assign the MS to an
	 * {@link AttractionPoint}.
	 * 
	 * @param ms
	 */
	public void addComponent(SimLocationActuator ms);

}
