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

package de.tud.kom.p2psim.impl.topology.movement;

import java.util.Set;

import de.tud.kom.p2psim.api.topology.movement.SimLocationActuator;
import de.tud.kom.p2psim.impl.topology.PositionVector;

/**
 * The Movement Supported Components move to one Position. It is possible to set
 * the speed for every movement. <br>
 * The method setTimeBetweenMoveOperations must be called, to start the periodic
 * operation!
 * 
 * @author Christoph Muenker
 * @version 1.0, 21.11.2012
 */
public class TargetMovement extends AbstractMovementModel {
	private double x = 0;

	private double y = 0;

	private double distance;

	public TargetMovement() {
		super();
	}

	@Override
	public void move() {
		Set<SimLocationActuator> comps = getComponents();
		for (SimLocationActuator comp : comps) {
			PositionVector pos = comp.getRealPosition();
			if (Math.round(pos.getX()) == x && Math.round(pos.getY()) == y) {
				continue;
			}

			PositionVector rVec = new PositionVector(x, y);
			rVec = rVec.minus(pos);

			double length = rVec.distanceTo(new PositionVector(0, 0));

			PositionVector vec;
			if (length > distance) {
				vec = new PositionVector(rVec.getX() / length * distance,
						rVec.getY() / length * distance);
			} else {
				vec = rVec;
			}
			
			updatePosition(comp, pos.plus(vec));
		}
	}

	public void setX(double x) {
		this.x = x;
	}

	public void setY(double y) {
		this.y = y;
	}

	public void setDistancePerMovement(double distance) {
		this.distance = Math.abs(distance);
	}

}
