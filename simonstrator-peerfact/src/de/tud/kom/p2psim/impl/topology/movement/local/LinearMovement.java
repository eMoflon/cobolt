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

package de.tud.kom.p2psim.impl.topology.movement.local;

import de.tud.kom.p2psim.api.topology.movement.SimLocationActuator;
import de.tud.kom.p2psim.impl.topology.PositionVector;
import de.tud.kom.p2psim.impl.util.Either;
import de.tud.kom.p2psim.impl.util.Left;

/**
 * This movement strategy moves directly towards a given destination without
 * regard for obstacles or way points.
 * 
 * @author Fabio Zöllner
 * @version 1.0, 09.04.2012
 */
public class LinearMovement extends AbstractLocalMovementStrategy {

	public Either<PositionVector, Boolean> nextPosition(SimLocationActuator comp,
			PositionVector destination) {
		PositionVector newPosition;
		if (destination
				.distanceTo(comp.getRealPosition()) < getMovementSpeed(comp)) {
			newPosition = destination.clone();
		} else {
			newPosition = comp.getRealPosition().moveStep(destination, getMovementSpeed(comp));
		}
		return new Left<PositionVector, Boolean>(newPosition);
	}
}
