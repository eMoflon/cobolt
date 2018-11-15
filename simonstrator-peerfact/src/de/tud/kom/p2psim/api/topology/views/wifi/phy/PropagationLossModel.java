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

package de.tud.kom.p2psim.api.topology.views.wifi.phy;

import de.tudarmstadt.maki.simonstrator.api.component.sensor.location.Location;

/**
 * This interface is for the calculation of the propagation loss of an
 * electrical wave. This mean, it calculates the RX-Power of a signal in
 * correspondence of the distance. Additionally it offers the reversion of the
 * calculation, so you get the distance in dependent of txPower and rxPower.
 * 
 * <p>
 * 
 * This class based on NS3 (src/propagation/model/propagation-loss-model.cc).
 * 
 * @author Christoph Muenker
 * @version 1.0, 28.02.2013
 */
public abstract class PropagationLossModel {
	/**
	 * light speed in meter per seconds
	 */
	protected static final long lightspeed = 299792458;

	/**
	 * Gets the RX Power in dBm for the distance from a to b.
	 * 
	 * @param txPowerDbm
	 *            The TX Power
	 * @param a
	 *            The first position
	 * @param b
	 *            The second position
	 * @return The RX power in dBm
	 */
	public abstract double getRxPowerDbm(double txPowerDbm, Location a,
			Location b);

	/**
	 * Gets the RX Power in dBm for the distance.
	 * 
	 * @param txPowerDbm
	 *            The TX Power
	 * @param distance
	 *            The distance from the source to the receiver in meter.
	 * @return The RX power in dBm.
	 */
	public abstract double getRxPowerDbm(double txPowerDbm, double distance);

	/**
	 * Gets the distance, which accord to the txPower in dBm and rx Power in
	 * dBm. This mean, that the loss will be calculated in distance.
	 * 
	 * @param txPowerDbm
	 *            The txPower in dBm
	 * @param rxPowerDbm
	 *            The rxPower in dBm
	 * @return The distance in meter.
	 */
	public abstract double getDistance(double txPowerDbm, double rxPowerDbm);

	/**
	 * Sets the used frequency.
	 * 
	 * @param frequency
	 *            The used frequency.
	 */
	public abstract void setFrequency(long frequency);

	/**
	 * Calculates dBm to watt
	 * 
	 * @param dbm
	 *            The dBm, which should be calculated to watt
	 * @return The given dBm as watt
	 */
	public static double dbmToW(double dbm) {
		double mw = Math.pow(10.0, dbm / 10.0);
		return mw / 1000.0;
	}

	/**
	 * Calculates watt to dBm.
	 * 
	 * @param w
	 *            The watt, which should be calculated to dBm.
	 * @return The given watt as dBm.
	 */
	public static double wToDbm(double w) {
		double dbm = Math.log10(w * 1000.0) * 10.0;
		return dbm;
	}
}
