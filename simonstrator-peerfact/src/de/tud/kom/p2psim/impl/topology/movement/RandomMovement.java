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

package de.tud.kom.p2psim.impl.topology.movement;

import java.util.Set;

import de.tud.kom.p2psim.api.topology.movement.SimLocationActuator;
import de.tud.kom.p2psim.impl.topology.PositionVector;

/**
 * Mainly for testing: random movement
 * 
 * @author Bjoern Richerzhagen, adapted from ido.MoveModels
 * @version 1.0, mm/dd/2011
 */
public class RandomMovement extends AbstractMovementModel {

	public RandomMovement() {
		super();
	}

	@Override
	public void move() {
		Set<SimLocationActuator> comps = getComponents();
		for (SimLocationActuator comp : comps) {
			PositionVector pos = comp.getRealPosition();
			updatePosition(comp, pos.plus(getRandomDeltaWithinSpeed(pos, comp.getMinMovementSpeed(),
					comp.getMaxMovementSpeed())));
		}
	}





}
