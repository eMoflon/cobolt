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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import de.tud.kom.p2psim.api.topology.movement.MovementInformation;
import de.tud.kom.p2psim.api.topology.movement.SimLocationActuator;
import de.tud.kom.p2psim.impl.topology.PositionVector;
import de.tudarmstadt.maki.simonstrator.api.Time;

/**
 * A Random-Path Movement
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, mm/dd/2011
 */
public class RandomPathMovement extends AbstractMovementModel {

	private Map<SimLocationActuator, RandomPathMovementInfo> stateInfo;

	/**
	 * Maximal time a component walks into one direction
	 */
	public int maxStepsInOneDirection = 500;

	public RandomPathMovement() {
		super();
		stateInfo = new LinkedHashMap<SimLocationActuator, RandomPathMovement.RandomPathMovementInfo>();
	}

	@Override
	public void addComponent(SimLocationActuator component) {
		super.addComponent(component);
		stateInfo.put(component, new RandomPathMovementInfo());
	}

	public void setMaxStepsInOneDirection(int maxStepsInOneDirection) {
		this.maxStepsInOneDirection = maxStepsInOneDirection;
	}

	@Override
	public void move() {
		Set<SimLocationActuator> comps = getComponents();
		for (SimLocationActuator comp : comps) {
			PositionVector pos = comp.getRealPosition();
			RandomPathMovementInfo info = stateInfo.get(comp);
			if (info.getRemainingSteps() == 0 || info.getDelta() == null) {
				// assign new delta and new steps!
				assignNewMovementInfo(comp);
			}
			updatePosition(comp, pos.plus(info.getDelta()));
			info.setRemainingSteps(info.getRemainingSteps() - 1);
		}
	}

	protected void assignNewMovementInfo(SimLocationActuator comp) {
		RandomPathMovementInfo info = stateInfo.get(comp);

		PositionVector actPos = comp.getRealPosition();
		PositionVector delta = null;
		int steps = 1;
		PositionVector targetPos;

		do {
			steps = getRandomInt(
					1,
					(int) (maxStepsInOneDirection * Time.SECOND / getTimeBetweenMoveOperations())); // converge
			delta = getRandomDeltaWithinSpeed(actPos,
					comp.getMinMovementSpeed(),
					comp.getMaxMovementSpeed());
			double[] target = new double[comp.getRealPosition()
					.getDimensions()];
			for (int i = 0; i < target.length; i++) {
				target[i] = actPos.getEntry(i) + delta.getEntry(i) * steps;
			}
			targetPos = new PositionVector(target);
		} while (!isValidPosition(targetPos));

		info.setDelta(delta);
		info.setRemainingSteps(steps);
		// System.out.println(info);
	}

	/**
	 * Stores persistent Information on the Devices Path
	 * 
	 * @author Bjoern Richerzhagen
	 * @version 1.0, 05/10/2011
	 */
	protected class RandomPathMovementInfo implements MovementInformation {

		private int remainingSteps = 0;

		private PositionVector delta;

		public void setDelta(PositionVector delta) {
			this.delta = delta;
		}

		public PositionVector getDelta() {
			return delta;
		}

		public void setRemainingSteps(int steps) {
			this.remainingSteps = steps;
		}

		public int getRemainingSteps() {
			return remainingSteps;
		}

		@Override
		public String toString() {
			return "MovementInfo: d=" + delta.toString() + " steps="
					+ remainingSteps;
		}
	}
}
