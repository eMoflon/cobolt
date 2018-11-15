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
import java.util.PriorityQueue;
import java.util.Set;

import de.tud.kom.p2psim.api.topology.movement.MovementInformation;
import de.tud.kom.p2psim.api.topology.movement.SimLocationActuator;
import de.tud.kom.p2psim.impl.topology.PositionVector;
import de.tud.kom.p2psim.impl.util.stat.distributions.NormalDistribution;
import de.tudarmstadt.maki.simonstrator.api.Randoms;
import de.tudarmstadt.maki.simonstrator.api.Time;
import de.tudarmstadt.maki.simonstrator.api.component.sensor.location.Location;
import de.tudarmstadt.maki.simonstrator.api.util.Distribution;
import de.tudarmstadt.maki.simonstrator.api.util.XMLConfigurableConstructor;

/**
 * This is an adjusted copy of {@link GaussMarkovMovement} that also supports
 * building virtual borders
 */
public class GeoFencedGaussMarkovMovement extends AbstractMovementModel {

	private double alpha;

	private double edgeThreshold = 20;

	private Distribution distribution_s = null;

	private Distribution distribution_d = null;

	private Map<SimLocationActuator, GaussMarkovMovementInfo> stateInfos;

	private GeoFence currentGeoFence;

	private PriorityQueue<GeoFence> geoFences;

	/**
	 * 
	 * @param alpha
	 */
	@XMLConfigurableConstructor({ "alpha", "edgeThreshold" })
	public GeoFencedGaussMarkovMovement(double alpha, double edgeThreshold) {
		this.alpha = alpha;
		if (alpha > 1 || alpha < 0) {
			throw new AssertionError(
					"For GaussMarkovMovement alpha has to be set between zero and one.");
		}
		this.edgeThreshold = edgeThreshold;
		this.stateInfos = new HashMap<SimLocationActuator, GeoFencedGaussMarkovMovement.GaussMarkovMovementInfo>();

		geoFences = new PriorityQueue<>();

	}

	@Override
	public void move() {
		if (distribution_s == null) {
			distribution_s = new NormalDistribution(0, 1);
		}
		if (distribution_d == null) {
			distribution_d = new NormalDistribution(0, 1);
		}
		if (currentGeoFence == null) {
			final double worldSizeX = getWorldDimension(0);
			final double worldSizeY = getWorldDimension(1);
			currentGeoFence = new GeoFence(0L, worldSizeX, worldSizeY);
		} else if (!geoFences.isEmpty()
				&& Time.getCurrentTime() >= geoFences.peek().getTimestamp()) {
			currentGeoFence = geoFences.poll();
		}

		Set<SimLocationActuator> comps = getComponents();
		for (SimLocationActuator comp : comps) {
			if (!shallMove(comp)) {
				continue;
			}
			PositionVector pos = comp.getRealPosition();
			final PositionVector newPos;
			if (currentGeoFence.contains(pos)) {
				newPos = calculateNewPositionAccordingToGaussMarkov(comp);
			} else {
				newPos = moveClosestToGeoFence(comp);
			}
			if (isValidPosition(newPos)) {
				updatePosition(comp, newPos);
			}
		}
	}

	public void setGeoFence(final GeoFence geoFence) {
		this.geoFences.add(geoFence);
	}

	private PositionVector moveClosestToGeoFence(SimLocationActuator comp) {
		final PositionVector pos = comp.getRealPosition();
		final Location center = currentGeoFence.getCenter();
		final double distance = comp.getMaxMovementSpeed()
				* getTimeBetweenMoveOperations() / Time.SECOND;
		final float bearing = pos.bearingTo(center);
		final double oldX = pos.getEntry(0);
		final double oldY = pos.getEntry(1);
		final double newX = oldX + Math.cos(bearing) * distance;
		final double newY = oldY + Math.sin(bearing) * distance;
		return new PositionVector(newX, newY);
	}

	private PositionVector calculateNewPositionAccordingToGaussMarkov(
			SimLocationActuator comp) {
		// get old x and y
		final PositionVector pos = comp.getRealPosition();
		final GaussMarkovMovementInfo inf = stateInfos.get(comp);
		final double x_old = pos.getEntry(0);
		final double y_old = pos.getEntry(1);
		final double xMin = currentGeoFence.getXMin();
		final double xMax = currentGeoFence.getXMax();
		final double yMin = currentGeoFence.getYMin();
		final double yMax = currentGeoFence.getYMax();
		// position within threshold?
		if (x_old < xMin + edgeThreshold || x_old > xMax - edgeThreshold
				|| y_old < yMin + edgeThreshold
				|| y_old > yMax - edgeThreshold) {
			if (x_old < xMin + edgeThreshold && y_old > yMax - edgeThreshold) {
				inf.d_bar = Math.PI * 0.25; // 45°
			} else if (x_old < xMin + edgeThreshold
					&& y_old < yMin + edgeThreshold) {
				inf.d_bar = Math.PI * 1.75; // 315°
			} else if (x_old < xMin + edgeThreshold) {
				inf.d_bar = 0; // 0°
			} else if (x_old > xMax - edgeThreshold
					&& y_old > yMax - edgeThreshold) {
				inf.d_bar = Math.PI * 0.75; // 135°
			} else if (x_old > xMax - edgeThreshold
					&& y_old < yMin + edgeThreshold) {
				inf.d_bar = Math.PI * 1.25; // 225°
			} else if (x_old > xMax - edgeThreshold) {
				inf.d_bar = Math.PI; // 180°
			} else if (y_old < yMin + edgeThreshold) {
				inf.d_bar = Math.PI * 1.5; // 270°
			} else if (y_old > yMax - edgeThreshold) {
				inf.d_bar = Math.PI * 0.5; // 90°
			}
			inf.d_old = inf.d_bar;
		}

		double s_n = alpha * inf.s_old + (1 - alpha) * inf.s_bar
				+ Math.sqrt(1 - (alpha * alpha)) * distribution_s.returnValue();
		if (s_n > comp.getMaxMovementSpeed() * getTimeBetweenMoveOperations()
				/ Time.SECOND) {
			// Maximum speed
			s_n = comp.getMaxMovementSpeed() * getTimeBetweenMoveOperations()
					/ Time.SECOND;
		}
		if (s_n < 0) {
			s_n = 0;
		}
		double d_n = (alpha * inf.d_old + (1 - alpha) * inf.d_bar
				+ Math.sqrt(1 - (alpha * alpha))
						* distribution_d.returnValue());
		double x_n = x_old + inf.s_old * Math.cos(inf.d_old);
		double y_n = y_old - inf.s_old * Math.sin(inf.d_old);
		/* minus, because origin is in the upper left */
		inf.d_old = d_n;
		inf.s_old = s_n;
		PositionVector newPos = new PositionVector(x_n, y_n);
		return newPos;
	}

	protected boolean shallMove(SimLocationActuator comp) {
		return true;
	}

	@Override
	public void addComponent(SimLocationActuator component) {
		super.addComponent(component);
		GaussMarkovMovementInfo info = new GaussMarkovMovementInfo();
		double rand = Randoms.getRandom(GeoFencedGaussMarkovMovement.class)
				.nextDouble();
		info.d_bar = rand * 2 * Math.PI;
		info.d_old = info.d_bar;
		info.s_bar = component.getMaxMovementSpeed() * rand
				* getTimeBetweenMoveOperations() / Time.SECOND;
		info.s_old = rand * component.getMaxMovementSpeed()
				* getTimeBetweenMoveOperations() / Time.SECOND;
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

	public GeoFence getCurrentGeoFence() {
		return this.currentGeoFence;
	}

}
