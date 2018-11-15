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

package de.tud.kom.p2psim.impl.topology.views.wifi.phy.errormodel;

import de.tud.kom.p2psim.api.topology.views.wifi.phy.ErrorRateModel;
import de.tud.kom.p2psim.api.topology.views.wifi.phy.WifiMode;
import de.tud.kom.p2psim.api.topology.views.wifi.phy.WifiMode.WifiModulationClass;
import de.tud.kom.p2psim.impl.util.NotSupportedException;

/**
 * Implements the default methods and structures, which can be used from other
 * Rate Models.
 * 
 * @author Christoph Muenker
 * @version 1.0, 28.02.2013
 */
public abstract class AbstractErrorRateModel implements ErrorRateModel {

	@Override
	public final double calculateSnrThreshold(WifiMode txMode, double ber) {
		return calculateSnrThreshold(txMode, ber, 1);
	}

	/**
	 * Copied from NS3 errorRateModel.cc
	 */
	@Override
	public final double calculateSnrThreshold(WifiMode txMode, double per,
			int nbits) {
		// This is a very simple binary search.
		double low, high, precision;
		low = 1e-25;
		high = 1e25;
		precision = 1e-12;
		while (high - low > precision) {
			assert (high >= low) : "Violation of condition";
			double middle = low + (high - low) / 2;
			if ((1 - getChunkSuccessRate(txMode, middle, nbits)) > per) {
				low = middle;
			} else {
				high = middle;
			}
		}
		return low;
	}

	@Override
	public final double getChunkSuccessRate(WifiMode mode, double snr, int nbits) {
		WifiModulationClass modClass = mode.getModClass();
		if (modClass == WifiModulationClass.WIFI_MOD_CLASS_ERP_OFDM
				|| modClass == WifiModulationClass.WIFI_MOD_CLASS_OFDM) {
			switch (mode.getConstellationSize()) {
			case 2:
				switch (mode.getCodingRate()) {
				case WIFI_CODE_RATE_1_2:
					return getOfdmFecBpskCodeRate1_2SuccessRate(mode, snr,
							nbits);
				case WIFI_CODE_RATE_3_4:
					return getOfdmFecBpskCodeRate3_4SuccessRate(mode, snr,
							nbits);
				}
				break;
			case 4:
				switch (mode.getCodingRate()) {
				case WIFI_CODE_RATE_1_2:
					return getOfdmFecQpskCodeRate1_2SuccessRate(mode, snr,
							nbits);
				case WIFI_CODE_RATE_3_4:
					return getOfdmFecQpskCodeRate3_4SuccessRate(mode, snr,
							nbits);
				}
				break;
			case 16:
				switch (mode.getCodingRate()) {
				case WIFI_CODE_RATE_1_2:
					return getOfdmFecQamCodeRate1_2SuccessRate(mode, snr, nbits);
				case WIFI_CODE_RATE_3_4:
					return getOfdmFecQamCodeRate3_4SuccessRate(mode, snr, nbits);
				}
				break;
			case 64:
				switch (mode.getCodingRate()) {
				case WIFI_CODE_RATE_2_3:
					return getOfdmFecQam64CodeRate2_3SuccessRate(mode, snr,
							nbits);
				case WIFI_CODE_RATE_3_4:
					return getOfdmFecQam64CodeRate3_4SuccessRate(mode, snr,
							nbits);
				case WIFI_CODE_RATE_5_6:
					return getOfdmFecQam64CodeRate5_6SuccessRate(mode, snr,
							nbits);
				}
				break;
			default:
				throw new NotSupportedException("The constellation Size "
						+ mode.getConstellationSize()
						+ " is not supported in the Error Rate Model!");
			}
		} else if (modClass == WifiModulationClass.WIFI_MOD_CLASS_DSSS) {
			switch (mode.getDataRate()) {
			case 1000000:
				return getDsssDbpskSuccessRate(mode, snr, nbits);
			case 2000000:
				return getDsssDqpskSuccessRate(mode, snr, nbits);
			case 5500000:
				return getDsssDqpskCck5_5SuccessRate(mode, snr, nbits);
			case 11000000:
				return getDsssDqpskCck11SuccessRate(mode, snr, nbits);
			}
		}
		throw new NotSupportedException("The Modulation class " + modClass
				+ " is not supported from Error Rate Model!");
	}

	// DSSS 1Mbit
	protected abstract double getDsssDbpskSuccessRate(WifiMode mode,
			double snr, int nbits);

	// DSSS 2Mbit
	protected abstract double getDsssDqpskSuccessRate(WifiMode mode,
			double snr, int nbits);

	// DSSS 5.5Mbit
	protected abstract double getDsssDqpskCck5_5SuccessRate(WifiMode mode,
			double snr, int nbits);

	// DSSS 11Mbit
	protected abstract double getDsssDqpskCck11SuccessRate(WifiMode mode,
			double snr, int nbits);

	// OFDM ConstelationSize 2 and CodeRate1_2
	protected abstract double getOfdmFecBpskCodeRate1_2SuccessRate(
			WifiMode mode, double snr, int nbits);

	// OFDM ConstelationSize 2 and CodeRate3_4
	protected abstract double getOfdmFecBpskCodeRate3_4SuccessRate(
			WifiMode mode, double snr, int nbits);

	// OFDM ConstelationSize 4 and CodeRate1_2
	protected abstract double getOfdmFecQpskCodeRate1_2SuccessRate(
			WifiMode mode, double snr, int nbits);

	// OFDM ConstelationSize 4 and CodeRate3_4
	protected abstract double getOfdmFecQpskCodeRate3_4SuccessRate(
			WifiMode mode, double snr, int nbits);

	// OFDM ConstelationSize 16 and CodeRate1_2
	protected abstract double getOfdmFecQamCodeRate1_2SuccessRate(
			WifiMode mode, double snr, int nbits);

	// OFDM ConstelationSize 16 and CodeRate3_4
	protected abstract double getOfdmFecQamCodeRate3_4SuccessRate(
			WifiMode mode, double snr, int nbits);

	// OFDM ConstelationSize 64 and CodeRate2_3
	protected abstract double getOfdmFecQam64CodeRate2_3SuccessRate(
			WifiMode mode, double snr, int nbits);

	// OFDM ConstelationSize 64 and CodeRate3_4
	protected abstract double getOfdmFecQam64CodeRate3_4SuccessRate(
			WifiMode mode, double snr, int nbits);

	// OFDM ConstelationSize 64 and CodeRate5_6
	protected abstract double getOfdmFecQam64CodeRate5_6SuccessRate(
			WifiMode mode, double snr, int nbits);

}
