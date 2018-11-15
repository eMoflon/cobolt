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

package de.tud.kom.p2psim.impl.topology.views.wifi.phy.propagation.loss;

import de.tud.kom.p2psim.api.topology.views.wifi.phy.PropagationLossModel;
import de.tudarmstadt.maki.simonstrator.api.component.sensor.location.Location;

/**
 * The Friis Propagation Loss Model:
 * 
 * <pre>
 *  P     Gt * Gr * (lambda^2)
 * --- = --------------------- 
 * Pt     (4 * pi * d)^2 * L
 * </pre>
 * 
 * Where:<br>
 * 
 * Pt: tx power (W)<br>
 * Gt: tx gain (unit-less) <br>
 * Gr: rx gain (unit-less)<br>
 * lambda: wavelength (m)<br>
 * d: distance (m)<br>
 * L: system loss <br>
 * 
 * 
 * Here, we set tx and rx gain to 1, so it has no influence.
 * 
 * <pre>
 *                 lambda^2
 * rx = tx * (-------------------) 
 *             (4 * pi * d)^2 * L
 * </pre>
 * 
 * <br>
 * 
 * This class based on NS3 (src/propagation/model/propagation-loss-model.cc).
 * 
 * @version 1.0, 21.08.2012
 */
public class FriisPropagationLossModel extends PropagationLossModel {

	/**
	 * Frequency in Hz<br>
	 * Default 2,4 GHz
	 */
	private long frequency = 2400000000l;

	/**
	 * The wavelength
	 */
	private double lambda = ((double) lightspeed) / frequency;

	/**
	 * The system loss
	 */
	private double systemLoss = 1d;

	/**
	 * The distance under which the propagation model refuse the result.<br>
	 * In meter.
	 */
	private double minDistance = 0.5d;

	@Override
	public double getRxPowerDbm(double txPowerDbm, Location a, Location b) {
		double distance = a.distanceTo(b);
		return getRxPowerDbm(txPowerDbm, distance);
	}

	@Override
	public double getRxPowerDbm(double txPowerDbm, double distance) {
		if (distance <= minDistance) {
			return txPowerDbm;
		}
		double recPower = dbmToW(txPowerDbm) * (lambda * lambda)
				/ (Math.pow((4 * Math.PI * distance), 2) * systemLoss);
		return wToDbm(recPower);

	}

	@Override
	public double getDistance(double txPowerDbm, double rxPowerDbm) {
		return Math.sqrt(dbmToW(txPowerDbm) * lambda * lambda
				/ (dbmToW(rxPowerDbm) * systemLoss * 16 * Math.PI * Math.PI));
	}

	@Override
	public void setFrequency(long frequency) {
		this.frequency = frequency;
		this.lambda = ((double) lightspeed) / frequency;
	}

	public void setSystemLoss(double systemLoss) {
		this.systemLoss = systemLoss;
	}

}
