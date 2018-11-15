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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.tud.kom.p2psim.api.topology.movement.MovementInformation;
import de.tud.kom.p2psim.api.topology.movement.SimLocationActuator;
import de.tud.kom.p2psim.impl.topology.PositionVector;
import de.tud.kom.p2psim.impl.util.stat.distributions.NormalDistribution;
import de.tudarmstadt.maki.simonstrator.api.Randoms;
import de.tudarmstadt.maki.simonstrator.api.Time;
import de.tudarmstadt.maki.simonstrator.api.util.Distribution;
import de.tudarmstadt.maki.simonstrator.api.util.XMLConfigurableConstructor;

/**
 * A movement Model that produces more realistic paths for mobile users, without
 * sharp edges or sudden motion changes. It is described in <i>A Survey of
 * Mobility Models for Ad Hoc Network Research</i> by Tracy Camp et al.
 * 
 * The notation used in this class is the same as in the paper
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 05/31/2011
 */
public class GaussMarkovMovement extends AbstractMovementModel {

	private double alpha;

	private double edgeThreshold = 20;

	private Distribution distribution_s = null;

	private Distribution distribution_d = null;

	private Map<SimLocationActuator, GaussMarkovMovementInfo> stateInfos;

	/**
	 * 
	 * @param alpha
	 */
	@XMLConfigurableConstructor({ "alpha", "edgeThreshold" })
	public GaussMarkovMovement(double alpha, double edgeThreshold) {
		this.alpha = alpha;
		if (alpha > 1 || alpha < 0) {
			throw new AssertionError(
					"For GaussMarkovMovement alpha has to be set between zero and one.");
		}
		this.edgeThreshold = edgeThreshold;
		this.stateInfos = new HashMap<SimLocationActuator, GaussMarkovMovement.GaussMarkovMovementInfo>();
	}

	@Override
	public void move() {
		if (distribution_s == null) {
			distribution_s = new NormalDistribution(0, 1);
		}
		if (distribution_d == null) {
			distribution_d = new NormalDistribution(0, 1);
		}
		Set<SimLocationActuator> comps = getComponents();
		for (SimLocationActuator comp : comps) {
			if (!shallMove(comp)) {
				continue;
			}
			// get old x and y
			PositionVector pos = comp.getRealPosition();
			GaussMarkovMovementInfo inf = stateInfos.get(comp);
			double x_old = pos.getEntry(0);
			double y_old = pos.getEntry(1);
			// position within threshold?
			if (x_old < edgeThreshold
					|| x_old > getWorldDimension(0) - edgeThreshold
					|| y_old < edgeThreshold
					|| y_old > getWorldDimension(1) - edgeThreshold) {
				if (x_old < edgeThreshold
						&& y_old > getWorldDimension(1) - edgeThreshold) {
					// System.err.println(inf.d_bar + " bl to 45");
					// inf.d_bar = 45;
					inf.d_bar = Math.PI * 0.25;
				} else if (x_old < edgeThreshold && y_old < edgeThreshold) {
					// System.err.println(inf.d_bar + " tl to 315");
					// inf.d_bar = 315;
					inf.d_bar = Math.PI * 1.75;
				} else if (x_old < edgeThreshold) {
					// System.err.println(inf.d_bar + " l to 0");
					// inf.d_bar = 0;
					inf.d_bar = 0;
				} else if (x_old > getWorldDimension(0) - edgeThreshold
						&& y_old > getWorldDimension(1) - edgeThreshold) {
					// System.err.println(inf.d_bar + " br to 135");
					// inf.d_bar = 135;
					inf.d_bar = Math.PI * 0.75;
				} else if (x_old > getWorldDimension(0) - edgeThreshold
						&& y_old < edgeThreshold) {
					// System.err.println(inf.d_bar + " tr to 225");
					// inf.d_bar = 225;
					inf.d_bar = Math.PI * 1.25;
				} else if (x_old > getWorldDimension(0) - edgeThreshold) {
					// System.err.println(inf.d_bar + " r to 180");
					// inf.d_bar = 180;
					inf.d_bar = Math.PI;
				} else if (y_old < edgeThreshold) {
					// System.err.println(inf.d_bar + " t to 270");
					// inf.d_bar = 270;
					inf.d_bar = Math.PI * 1.5;
				} else if (y_old > getWorldDimension(1) - edgeThreshold) {
					// System.err.println(inf.d_bar + " b to 90");
					// inf.d_bar = 90;
					inf.d_bar = Math.PI * 0.5;
				}
				inf.d_old = inf.d_bar;
			}

			double s_n = alpha * inf.s_old + (1 - alpha) * inf.s_bar
					+ Math.sqrt(1 - (alpha * alpha))
					* distribution_s.returnValue();
			if (s_n > comp.getMaxMovementSpeed()
					* getTimeBetweenMoveOperations()
					/ Time.SECOND) {
				// Maximum speed
				s_n = comp.getMaxMovementSpeed()
						* getTimeBetweenMoveOperations()
						/ Time.SECOND;
			}
			if (s_n < 0) {
				s_n = 0;
			}
			//System.out.println(inf.d_old + " " + inf.d_bar);
			double d_n = (alpha * inf.d_old + (1 - alpha) * inf.d_bar + Math
					.sqrt(1 - (alpha * alpha)) * distribution_d.returnValue());
			// if (d_n < 0) {
			// d_n += 2 * Math.PI;
			// }
			// if (d_n >= 2 * Math.PI) {
			// d_n -= 2 * Math.PI;
			// }
			double x_n = x_old + inf.s_old * Math.cos(inf.d_old);
			double y_n = y_old - inf.s_old * Math.sin(inf.d_old);
			/* minus, because origin is in the upper left */
			inf.d_old = d_n;
			inf.s_old = s_n;
			PositionVector newPos = new PositionVector(x_n, y_n);
			if (isValidPosition(newPos)) {
				updatePosition(comp, newPos);
			}
		}
	}

	protected boolean shallMove(SimLocationActuator comp) {
		return true;
	}

	@Override
	public void addComponent(SimLocationActuator component) {
		super.addComponent(component);
		GaussMarkovMovementInfo info = new GaussMarkovMovementInfo();
		double rand = Randoms.getRandom(GaussMarkovMovement.class).nextDouble();
		info.d_bar = rand * 2 * Math.PI;
		info.d_old = info.d_bar;
		info.s_bar = component.getMaxMovementSpeed() * rand
				* getTimeBetweenMoveOperations()
				/ Time.SECOND;
		info.s_old = rand * component.getMaxMovementSpeed()
				* getTimeBetweenMoveOperations()
				/ Time.SECOND;
		stateInfos.put(component, info);
	}

	/**
	 * This allows to overwrite the distribution for directions
	 * 
	 * @param distribution
	 */
	public void setDirectionDistribution(Distribution distribution) {
		this.distribution_d = distribution;
	}

	/**
	 * This allows to overwrite the distribution for speed
	 * 
	 * @param distribution
	 */
	public void setSpeedDistribution(Distribution distribution) {
		this.distribution_s = distribution;
	}

	/**
	 * Movement State Information for this movement model
	 * 
	 * @author Bjoern Richerzhagen
	 * @version 1.0, 05/31/2011
	 */
	private class GaussMarkovMovementInfo implements MovementInformation {

		public GaussMarkovMovementInfo() {
			// ntd
		}

		public double s_old = 0;

		public double d_old = 0;

		public double d_bar;

		public double s_bar;

	}

}
