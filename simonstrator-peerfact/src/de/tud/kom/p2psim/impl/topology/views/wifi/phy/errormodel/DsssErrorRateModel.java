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

import de.tud.kom.p2psim.api.topology.views.wifi.phy.WifiMode;
import de.tud.kom.p2psim.impl.util.NotSupportedException;

/**
 * Implements the DSSS Errpr Rate Model. It use only the fitted Matlab version,
 * because the other Model was not efficient in Java (infinity integral
 * problem.)<br>
 * It implements only the DSSS getter. The other getter throws
 * {@link NotSupportedException}s.
 * 
 * The formulas are copied from NS3 (src/wifi/model/dsss-error-rate-model.cc)
 * 
 * @author Christoph Muenker
 * @version 1.0, 28.02.2013
 */
public class DsssErrorRateModel extends AbstractErrorRateModel {

	static double WLAN_SIR_PERFECT = 10.0;

	static double WLAN_SIR_IMPOSSIBLE = 0.1;

	@Override
	protected double getDsssDbpskSuccessRate(WifiMode mode, double snr,
			int nbits) {
		double EbN0 = snr * 22000000.0 / 1000000.0; // 1 bit per symbol with 1
													// MSPS
		double ber = 0.5 * Math.exp(-EbN0);
		return Math.pow((1.0 - ber), nbits);
	}

	@Override
	protected double getDsssDqpskSuccessRate(WifiMode mode, double snr,
			int nbits) {
		double EbN0 = snr * 22000000.0 / 1000000.0 / 2.0; // 2 bits per symbol,
															// 1 MSPS
		double ber = dqpskFunction(EbN0);
		return Math.pow((1.0 - ber), nbits);
	}

	@Override
	protected double getDsssDqpskCck5_5SuccessRate(WifiMode mode, double snr,
			int nbits) {
		// NS_LOG_WARN("Running a 802.11b CCK Matlab model less accurate than GSL model");
		// The matlab model
		double ber;
		if (snr > WLAN_SIR_PERFECT) {
			ber = 0.0;
		} else if (snr < WLAN_SIR_IMPOSSIBLE) {
			ber = 0.5;
		} else {
			// fitprops.coeff from matlab berfit
			double a1 = 5.3681634344056195e-001;
			double a2 = 3.3092430025608586e-003;
			double a3 = 4.1654372361004000e-001;
			double a4 = 1.0288981434358866e+000;
			ber = a1 * Math.exp(-(Math.pow((snr - a2) / a3, a4)));
		}
		return Math.pow((1.0 - ber), nbits);

	}

	@Override
	protected double getDsssDqpskCck11SuccessRate(WifiMode mode, double snr,
			int nbits) {
		// NS_LOG_WARN
		// ("Running a 802.11b CCK Matlab model less accurate than GSL model");
		// The matlab model
		double ber;
		if (snr > WLAN_SIR_PERFECT) {
			ber = 0.0;
		} else if (snr < WLAN_SIR_IMPOSSIBLE) {
			ber = 0.5;
		} else {
			// fitprops.coeff from matlab berfit
			double a1 = 7.9056742265333456e-003;
			double a2 = -1.8397449399176360e-001;
			double a3 = 1.0740689468707241e+000;
			double a4 = 1.0523316904502553e+000;
			double a5 = 3.0552298746496687e-001;
			double a6 = 2.2032715128698435e+000;
			ber = (a1 * snr * snr + a2 * snr + a3)
					/ (snr * snr * snr + a4 * snr * snr + a5 * snr + a6);
		}
		return Math.pow((1.0 - ber), nbits);
	}

	private double dqpskFunction(double x) {
		double sqrt2 = Math.sqrt(2);
		return ((sqrt2 + 1.0) / Math.sqrt(8.0 * 3.1415926 * sqrt2))
				* (1.0 / Math.sqrt(x)) * Math.exp(-(2.0 - sqrt2) * x);
	}

	@Override
	protected double getOfdmFecBpskCodeRate1_2SuccessRate(WifiMode mode,
			double snr, int nbits) {
		throw new NotSupportedException(
				"The DsssErrorRateModel doesn't support OFDM Modulation!!");
	}

	@Override
	protected double getOfdmFecBpskCodeRate3_4SuccessRate(WifiMode mode,
			double snr, int nbits) {
		throw new NotSupportedException(
				"The DsssErrorRateModel doesn't support OFDM Modulation!!");
	}

	@Override
	protected double getOfdmFecQpskCodeRate1_2SuccessRate(WifiMode mode,
			double snr, int nbits) {
		throw new NotSupportedException(
				"The DsssErrorRateModel doesn't support OFDM Modulation!!");
	}

	@Override
	protected double getOfdmFecQpskCodeRate3_4SuccessRate(WifiMode mode,
			double snr, int nbits) {
		throw new NotSupportedException(
				"The DsssErrorRateModel doesn't support OFDM Modulation!!");
	}

	@Override
	protected double getOfdmFecQamCodeRate1_2SuccessRate(WifiMode mode,
			double snr, int nbits) {
		throw new NotSupportedException(
				"The DsssErrorRateModel doesn't support OFDM Modulation!!");
	}

	@Override
	protected double getOfdmFecQamCodeRate3_4SuccessRate(WifiMode mode,
			double snr, int nbits) {
		throw new NotSupportedException(
				"The DsssErrorRateModel doesn't support OFDM Modulation!!");
	}

	@Override
	protected double getOfdmFecQam64CodeRate2_3SuccessRate(WifiMode mode,
			double snr, int nbits) {
		throw new NotSupportedException(
				"The DsssErrorRateModel doesn't support OFDM Modulation!!");
	}

	@Override
	protected double getOfdmFecQam64CodeRate3_4SuccessRate(WifiMode mode,
			double snr, int nbits) {
		throw new NotSupportedException(
				"The DsssErrorRateModel doesn't support OFDM Modulation!!");
	}

	@Override
	protected double getOfdmFecQam64CodeRate5_6SuccessRate(WifiMode mode,
			double snr, int nbits) {
		throw new NotSupportedException(
				"The DsssErrorRateModel doesn't support OFDM Modulation!!");
	}

}
