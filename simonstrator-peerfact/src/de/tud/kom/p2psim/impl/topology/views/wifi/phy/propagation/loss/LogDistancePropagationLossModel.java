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
 * 
 * 
 * The formula is:
 * <p>
 * rx = tx - Pr0 - 10 * n * log (d/d0)
 * <p>
 * Pr0: rx power at reference distance d0 (dBm) <br>
 * d0: reference distance: 1.0 (m) <br>
 * d: distance (m) <br>
 * n: the path loss exponent <br>
 * tx: tx power (dBm) <br>
 * rx: rx power (dBm)
 * 
 * 
 * This class based on NS3 (src/propagation/model/propagation-loss-model.cc) by
 * Mathieu Lacage <mathieu.lacage@sophia.inria.fr> and further extended by
 * Christoph Muenker.
 * 
 * @version 1.0, 28.02.2013
 */
public class LogDistancePropagationLossModel extends PropagationLossModel {

	/**
	 * Exponent of the path loss propagation Model. In
	 * "Wireless Communications - Principles and Practices" from Theodore S.
	 * Rappaport different exponents are introduced. To model the propagation
	 * loss of an urban area cellular radio, a value between 2.7 and 3.5 should
	 * be chosen.
	 */
	private double exponent = 3.2;

	/**
	 * The reference distance of the referenceLoss
	 */
	private double referenceDistance = 1;

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
	 * The reference Loss
	 */
	private double referenceLoss = 20 * Math.log10(4 * Math.PI
			* referenceDistance / lambda);

	@Override
	public double getRxPowerDbm(double txPowerDbm, Location a, Location b) {
		double distance = a.distanceTo(b);
		return getRxPowerDbm(txPowerDbm, distance);
	}

	@Override
	public double getRxPowerDbm(double txPowerDbm, double distance) {
		if (distance <= referenceDistance) {
			return txPowerDbm;
		}
		double pathLoss = 10 * exponent
				* Math.log10(distance / referenceDistance);

		double rxc = -this.referenceLoss - pathLoss;
		return txPowerDbm + rxc;
	}

	@Override
	public double getDistance(double txPowerDbm, double rxPowerDbm) {
		return Math.pow(10, (rxPowerDbm - txPowerDbm + referenceLoss)
				/ (-10 * exponent))
				/ referenceDistance;
	}

	@Override
	public void setFrequency(long frequency) {
		this.frequency = frequency;
		updatePreComputation();
	}

	public void setReferenceDistance(double referenceDistance) {
		this.referenceDistance = referenceDistance;
		updatePreComputation();
	}

	public void setExponent(double exponent) {
		this.exponent = exponent;
	}

	public double getExponent() {
		return exponent;
	}

	/**
	 * The lamba and the referenceLoss is depending of the frequency and the
	 * referenceDistance. If one of this values changed, then should be updated
	 * the pre computed values.
	 */
	private void updatePreComputation() {
		this.lambda = ((double) lightspeed) / frequency;
		this.referenceLoss = 20 * Math.log10(4 * Math.PI * referenceDistance
				/ lambda);
	}
}
