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

package de.tud.kom.p2psim.impl.topology.movement.modularosm.transition;

import java.util.List;
import java.util.Map;

import de.tud.kom.p2psim.api.topology.movement.SimLocationActuator;
import de.tud.kom.p2psim.impl.topology.movement.modularosm.attraction.AttractionPoint;

/**
 * This is the interface for the Transition Strategy.<br>
 * 
 * @author Martin Hellwig
 * @version 1.0, 03.07.2015
 */
public interface ITransitionStrategy {
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
	 * Add the object and assign the MS to an {@link AttractionPoint}.
	 * 
	 * @param ms
	 */
	public void addComponent(SimLocationActuator ms);

	/**
	 * Notify the TransitionStrategy, that the component has reached an
	 * attraction point.
	 * 
	 * @param ms
	 */
	public void reachedAttractionPoint(SimLocationActuator ms);

	/**
	 * Updates the target attraction point of a component
	 * 
	 * @param attractionPoint
	 */
	public void updateTargetAttractionPoint(SimLocationActuator comp,
			AttractionPoint attractionPoint);

}
